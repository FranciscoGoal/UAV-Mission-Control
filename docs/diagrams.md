# Mission Control Technical Diagrams

These diagrams describe the implementation currently present in the repository. Planned components from the roadmap are deliberately excluded.

## System Architecture

The implemented UAV use cases are available only to internal Java callers. Command storage and STOMP infrastructure are Spring components, but neither is connected to an application-level messaging workflow.

```mermaid
flowchart TB
    subgraph External[External]
        StompClient[STOMP client]
    end

    subgraph Infrastructure[Infrastructure]
        Endpoint[WebSocket endpoint /ws]
        AppPrefix[Application prefix /app]
        Broker[In-process simple broker<br/>/topic and /queue]
        UserPrefix[User destination prefix /user]
        UavAdapter[InMemoryUavRepository]
        CommandAdapter[InMemoryCommandRepository]
        ExecutionAdapter[InMemoryCommandExecutionRepository]
    end

    subgraph Application[Application]
        InternalCaller[Internal Java caller]
        UavService[UavService]
        NoHandlers[No message handlers or publishers]
    end

    subgraph Domain[Domain]
        UavModel[Uav and Position]
        CommandModel[UavCommand implementations]
        ExecutionModel[CommandExecution]
        UavRepository[UavRepository]
        CommandRepository[CommandRepository]
        ExecutionRepository[CommandExecutionRepository]
    end

    StompClient <--> Endpoint
    Endpoint --> AppPrefix
    Endpoint --> Broker
    Endpoint --> UserPrefix
    AppPrefix -. no matching application destinations .-> NoHandlers

    InternalCaller --> UavService
    UavService --> UavModel
    UavService --> UavRepository
    UavAdapter -. implements .-> UavRepository

    CommandModel --> CommandRepository
    ExecutionModel --> ExecutionRepository
    CommandAdapter -. implements .-> CommandRepository
    ExecutionAdapter -. implements .-> ExecutionRepository
```

The repositories use concurrent maps and retain only process-local state. There is no database, external broker, telemetry history, or transactional relationship among the three repositories.

## Command Interaction

There is no implemented sequence from external command receipt to UAV execution. In particular, the code has no command controller or message handler, command application service, dispatcher, ACK consumer, result publisher, retry worker, or expiration scheduler.

The sequence below is the command behavior that can currently be invoked directly in Java: construct a validated command, store it, and optionally create and store an execution snapshot.

```mermaid
sequenceDiagram
    actor Caller as Internal Java caller
    participant Command as Concrete command
    participant Commands as CommandRepository
    participant Execution as CommandExecution
    participant Executions as CommandExecutionRepository

    Caller->>Command: construct(...) or create(...)
    activate Command
    Command->>Command: validate common and type-specific fields
    alt invalid command data
        Command-->>Caller: NullPointerException or IllegalArgumentException
    else valid command data
        Command-->>Caller: UavCommand
    end
    deactivate Command

    Caller->>Commands: save(command)
    Commands-->>Caller: stored command

    opt Caller records an execution snapshot
        Caller->>Execution: new(command, status, session, parameters, updatedAt)
        Execution->>Execution: validate non-null fields and copy parameters
        Execution-->>Caller: execution snapshot
        Caller->>Executions: save(execution)
        alt first snapshot for command ID
            Executions-->>Caller: stored execution
        else same command and non-older timestamp
            Executions-->>Caller: replaced execution
        else changed command or older timestamp
            Executions-->>Caller: IllegalArgumentException
        end
    end

    Note over Caller,Executions: No transport receipt, UAV dispatch, ACK, result notification, or automatic expiration is implemented.
```

## Telemetry Flow

The repository does not define a telemetry message or external telemetry source. Its only telemetry-like operation is an internal position update that replaces the latest `Position` stored for a UAV.

```mermaid
sequenceDiagram
    actor Caller as Internal Java caller
    participant Position as Position
    participant Service as UavService
    participant Repository as UavRepository
    participant Uav as Uav

    Caller->>Position: new(latitude, longitude, altitude)
    Position->>Position: validate coordinate ranges and finite values
    alt invalid position
        Position-->>Caller: IllegalArgumentException
    else valid position
        Position-->>Caller: position
        Caller->>Service: updatePosition(uavId, position)
        Service->>Repository: findById(uavId)
        alt UAV is not registered
            Repository-->>Service: Optional.empty
            Service-->>Caller: IllegalArgumentException
        else UAV exists
            Repository-->>Service: Uav
            Service->>Uav: updatePosition(position)
            Service->>Repository: save(uav)
            Repository-->>Service: updated Uav
            Service-->>Caller: updated Uav
        end
    end

    Note over Caller,Uav: No timestamped telemetry ingestion, history, event publication, or consumer distribution exists.
```

## UAV Status Behavior

`UavStatus` declares six values, but the implementation does not enforce a flight-state machine. Registration accepts any non-null value and `updateStatus` permits every value-to-value transition, including self-transitions.

```mermaid
stateDiagram-v2
    state "Any declared UAV status" as AnyStatus
    state "Rejected update" as Rejected

    [*] --> AnyStatus: register with caller-supplied status
    AnyStatus --> AnyStatus: updateStatus(any non-null value)
    AnyStatus --> Rejected: updateStatus(null)
    Rejected --> AnyStatus: stored state is unchanged

    note right of AnyStatus
        GROUND
        TAKING_OFF
        FLYING
        RETURNING_HOME
        LANDING
        DISCONNECTED
    end note
```

An update for an unknown UAV is rejected by `UavService` before the entity is modified. Position and altitude do not constrain status changes.

## Command Status Behavior

`CommandStatus` supplies lifecycle vocabulary, not an enforced lifecycle. A `CommandExecution` can be created with any declared status, and the repository does not compare previous and next statuses.

```mermaid
stateDiagram-v2
    state "Any declared command status" as AnyStatus
    state "Save rejected" as Rejected

    [*] --> AnyStatus: construct with any non-null status
    AnyStatus --> AnyStatus: same command and updatedAt >= stored updatedAt
    AnyStatus --> Rejected: changed command definition
    AnyStatus --> Rejected: updatedAt < stored updatedAt
    Rejected --> AnyStatus: stored snapshot is unchanged

    note right of AnyStatus
        PENDING
        SENT
        ACKNOWLEDGED
        COMPLETED
        FAILED
        EXPIRED
    end note
```

There are no terminal-state guards, automatic transitions, timeout processing, or execution history. The repository keeps only the latest accepted snapshot for each command ID.

## UAV Domain Model

```mermaid
classDiagram
    class Uav {
        -UUID id
        -Position position
        -UavStatus status
        +getId() UUID
        +getPosition() Position
        +getStatus() UavStatus
        +updatePosition(Position)
        +updateStatus(UavStatus)
    }

    class Position {
        +double latitude
        +double longitude
        +double altitude
    }

    class UavStatus {
        <<enumeration>>
        GROUND
        TAKING_OFF
        FLYING
        RETURNING_HOME
        LANDING
        DISCONNECTED
    }

    class UavService {
        +registerUav(UUID, Position, UavStatus) Uav
        +getUav(UUID) Uav
        +updatePosition(UUID, Position) Uav
        +updateStatus(UUID, UavStatus) Uav
    }

    class UavRepository {
        <<interface>>
        +save(Uav) Uav
        +findById(UUID) Optional~Uav~
        +findAll() List~Uav~
    }

    class InMemoryUavRepository

    Uav *-- Position : latest position
    Uav --> UavStatus : current status
    UavService --> Uav
    UavService --> UavRepository
    InMemoryUavRepository ..|> UavRepository
```

`Position` is immutable and validates latitude in `[-90, 90]`, longitude in `[-180, 180]`, and non-negative altitude. All values must be finite.

## Command Model

```mermaid
classDiagram
    class UavCommand {
        <<interface>>
        +id() UUID
        +uavId() String
        +type() CommandType
        +createdAt() Instant
        +expiresAt() Instant
    }

    class TakeOffCommand {
        +double targetAltitudeMeters
    }

    class GoToCommand {
        +Position targetPosition
    }

    class LandCommand
    class ReturnHomeCommand

    class CommandExecution {
        +UavCommand command
        +CommandStatus status
        +String operatorSesionId
        +List~String~ parameters
        +Instant updatedAt
    }

    class CommandType {
        <<enumeration>>
        TAKE_OFF
        GO_TO
        LAND
        RETURN_HOME
    }

    class CommandStatus {
        <<enumeration>>
        PENDING
        SENT
        ACKNOWLEDGED
        COMPLETED
        FAILED
        EXPIRED
    }

    class Uav {
        +UUID id
    }

    class Position
    class CommandRepository {
        <<interface>>
    }
    class CommandExecutionRepository {
        <<interface>>
    }

    UavCommand <|.. TakeOffCommand
    UavCommand <|.. GoToCommand
    UavCommand <|.. LandCommand
    UavCommand <|.. ReturnHomeCommand
    UavCommand --> CommandType
    GoToCommand *-- Position : target
    CommandExecution *-- UavCommand : embeds
    CommandExecution --> CommandStatus
    CommandRepository --> UavCommand : stores
    CommandExecutionRepository --> CommandExecution : stores latest snapshot
    UavCommand ..> Uav : unverified string identifier
```

The command-to-UAV relationship is conceptual only. Commands store `uavId` as a non-blank `String`, while `Uav` uses a `UUID`; no service currently resolves that string or verifies that the target UAV exists. `CommandExecution` embeds its command and has no independent execution identifier or result payload.

## Deliberately Omitted Views

Mission lifecycle, simulation, operator-interface, and database diagrams are not included because those subsystems do not exist in the current implementation. They belong to the roadmap and should be documented only after their contracts and behavior are implemented.
