# ADR-001: Require the Current UAV Status During Registration

- **Status:** Proposed
- **Date:** 2026-10-06
- **Decision owners:** Mission Control maintainers
- **Affected components:** Domain model, application service, tests, future STOMP contracts, and project documentation

## Context

The current `Uav` constructor receives an identifier and a position:

```java
public Uav(UUID id, Position position)
```

The implementation assigns `UavStatus.GROUND` automatically. The application service follows the same assumption through this registration method:

```java
public Uav registerUav(UUID id, Position initialPosition)
```

This behavior incorrectly treats registration in Mission Control as the creation or physical startup of a UAV. Registration only means that Mission Control has started tracking a UAV. The aircraft may already be operating when this happens.

Examples include:

- Mission Control restarting while a UAV remains in flight.
- A UAV reconnecting during an active mission.
- Control being transferred from another Mission Control instance.
- An aircraft beginning to publish telemetry after takeoff.
- A UAV returning home when it first becomes visible to this application.

In these scenarios, assigning `GROUND` creates a temporary state that does not represent reality. It could also result in incorrect notifications, invalid command decisions, or misleading operator information once real-time messaging is implemented.

## Decision

Registration must require the UAV's current operational status. Mission Control must not infer a default status.

The domain constructor will change to:

```java
public Uav(UUID id, Position position, UavStatus status)
```

The application service will change to:

```java
public Uav registerUav(
        UUID id,
        Position currentPosition,
        UavStatus currentStatus
)
```

All three values will be mandatory:

- `id` identifies the UAV.
- `currentPosition` describes its latest known location.
- `currentStatus` describes its operational state at registration time.

The constructor will reject a `null` status using `Objects.requireNonNull`, consistent with identifier and position validation.

No overloaded constructor with an implicit default will be retained. There are currently no external consumers that require backward compatibility, and keeping a default would preserve the ambiguity this decision is intended to remove.

## Supported Registration States

Registration will accept the statuses already defined by `UavStatus`:

| Status | Meaning at registration time |
|---|---|
| `GROUND` | The UAV is on the ground. |
| `TAKING_OFF` | The UAV is currently taking off. |
| `FLYING` | The UAV is airborne and operating. |
| `RETURNING_HOME` | The UAV is returning to its home position. |
| `LANDING` | The UAV is currently landing. |
| `DISCONNECTED` | The last known state indicates that communication is unavailable. |

This ADR does not define which component is authoritative for the reported state. Authentication, state reconciliation, reconnect semantics, and stale telemetry handling will be designed with the STOMP protocol.

## Domain API Changes

### Before

```java
Uav uav = new Uav(id, position);
```

The resulting status is inferred as `GROUND`.

### After

```java
Uav uav = new Uav(id, position, UavStatus.FLYING);
```

The resulting status is the state reported during registration.

The constructor implementation should follow this form:

```java
public Uav(UUID id, Position position, UavStatus status) {
    this.id = Objects.requireNonNull(id, "UAV id cannot be null");
    this.position = Objects.requireNonNull(position, "Position cannot be null");
    this.status = Objects.requireNonNull(status, "UAV status cannot be null");
}
```

The existing `updateStatus` behavior remains unchanged. Registration establishes the first state known by this Mission Control instance; later telemetry or state messages update it.

## Application Service Changes

`UavService.registerUav` must receive and forward the current status:

```java
public synchronized Uav registerUav(
        UUID id,
        Position currentPosition,
        UavStatus currentStatus
) {
    if (uavRepository.findById(id).isPresent()) {
        throw new IllegalArgumentException(
                "A UAV with this id already exists: " + id
        );
    }

    Uav uav = new Uav(id, currentPosition, currentStatus);
    return uavRepository.save(uav);
}
```

Duplicate registration behavior is not changed by this decision.

## Future STOMP Contract Impact

The project does not yet implement `@MessageMapping` handlers or registration DTOs. When the STOMP registration contract is introduced, the registration payload must include both current position and current status.

Conceptual payload:

```json
{
  "uavId": "00000000-0000-0000-0000-000000000001",
  "position": {
    "latitude": 42.28,
    "longitude": -8.73,
    "altitude": 120.0
  },
  "status": "FLYING"
}
```

The future STOMP adapter will be responsible for:

1. Deserializing the registration message.
2. Validating that all required fields are present.
3. Mapping the payload to `UUID`, `Position`, and `UavStatus`.
4. Calling `UavService.registerUav` with the reported values.
5. Publishing an acknowledgement or protocol-specific error.

No WebSocket destination names are established by this ADR.

## Test Changes

The test that assumes every UAV starts on the ground must be removed:

```java
void startsOnGround()
```

It will be replaced by parameterized or focused tests proving that registration preserves the supplied state.

Minimum domain test:

```java
@Test
void startsWithReportedStatus() {
    Uav uav = new Uav(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            new Position(42.28, -8.73, 120),
            UavStatus.FLYING
    );

    assertEquals(UavStatus.FLYING, uav.getStatus());
}
```

Required test coverage:

- A UAV can register as `GROUND`.
- A UAV can register as `FLYING`.
- A UAV can register in another active mission state such as `RETURNING_HOME`.
- A `null` registration status is rejected.
- `UavService` passes the supplied status to the entity.
- Duplicate registration remains rejected.
- Existing position and status update behavior remains valid.

## Documentation Changes

When this ADR is implemented, `README.md` must be updated to:

- Remove the invariant stating that the initial status is always `GROUND`.
- State that registration requires the current UAV position and status.
- Update the constructor and service signatures in the class diagram.
- Update the registration or messaging sequence diagrams.
- Include `status` in any future STOMP registration examples.

## Migration Plan

1. Change the `Uav` constructor to require `UavStatus`.
2. Change `UavService.registerUav` to require the current status.
3. Update every constructor and service call site.
4. Replace the default-ground test with reported-status tests.
5. Add service tests for registration state propagation.
6. Update the README domain invariants and diagrams.
7. Run `./mvnw clean test` to detect stale call sites and behavior regressions.
8. Use the new signature when implementing the STOMP registration handler.

## Consequences

### Benefits

- Mission Control stores a state that reflects the UAV's actual situation.
- UAVs can register or reconnect during active missions.
- The domain no longer conflates registration with physical startup.
- The future messaging contract becomes explicit and deterministic.
- Consumers cannot accidentally rely on an arbitrary default state.

### Costs

- Every caller must provide a status.
- Future STOMP registration messages require an additional field.
- The application must decide how to handle missing, stale, or untrusted status reports at the protocol boundary.

## Alternatives Considered

### Default Every UAV to `GROUND`

Rejected because registration can occur while a UAV is airborne or executing a mission.

### Add an `UNKNOWN` Default

Not selected for the current change. `UNKNOWN` would avoid reporting an incorrect physical state, but it would still allow registration without the information Mission Control needs. It may be introduced later if the protocol must support partial or stale telemetry.

### Infer Status from Altitude

Rejected because altitude alone cannot determine operational intent. A UAV at zero altitude may be taking off or landing, while an airborne UAV may be hovering, flying, or returning home.

### Keep an Overloaded Constructor That Defaults to `GROUND`

Rejected because it would preserve inconsistent behavior and allow new call sites to omit the current status.

## Out of Scope

This decision does not implement:

- Valid transitions between UAV statuses.
- Command handling or command-state relationships.
- STOMP message handlers.
- Authentication or authorization.
- Telemetry timestamps or stale-state detection.
- Persistence or state restoration.
- Reconnection conflict resolution.

These concerns should be addressed by separate decisions.

## Acceptance Criteria

This ADR is considered implemented when:

- `Uav` cannot be constructed without a non-null `UavStatus`.
- `UavService.registerUav` requires and preserves the reported status.
- No code path silently assigns `GROUND` during registration.
- A UAV can register with `FLYING` and retain that status.
- Tests cover active mission registration and null rejection.
- The README and diagrams match the new signatures.
- `./mvnw clean test` completes successfully.
