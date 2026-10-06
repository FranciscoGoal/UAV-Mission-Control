# ADR-001: Require the Current UAV Status During Registration

- **Status:** Accepted and implemented
- **Date:** 2026-10-06
- **Decision owners:** Mission Control maintainers
- **Affected components:** UAV domain model, application service, tests, and future external registration contracts

## Context

Registering a UAV means that Mission Control has started tracking it; it does not mean that the aircraft has just been created or started on the ground. A UAV may first become visible while flying, returning home, landing, or disconnected after Mission Control restarts or control is transferred.

Assigning `GROUND` automatically during registration would therefore introduce state that was not reported by the aircraft or caller. It would also make later messaging and command decisions depend on an arbitrary default.

## Decision

Registration requires the UAV's current position and operational status. Mission Control does not infer a default status.

The domain constructor is:

```java
public Uav(UUID id, Position position, UavStatus status)
```

The application operation is:

```java
public synchronized Uav registerUav(
        UUID id,
        Position initialPosition,
        UavStatus currentStatus
)
```

All three values are mandatory. The constructor rejects a null identifier, position, or status. No overload with an implicit status is retained.

Registration accepts every value currently declared by `UavStatus`:

- `GROUND`
- `TAKING_OFF`
- `FLYING`
- `RETURNING_HOME`
- `LANDING`
- `DISCONNECTED`

Duplicate identifiers remain invalid and are rejected by `UavService`.

## Implementation

The decision is implemented in `Uav` and `UavService`. Domain and service tests verify that the supplied status is preserved, null values are rejected, and duplicate registration remains invalid.

The implementation deliberately does not assign meaning to a status beyond storing the reported value. `updateStatus` accepts any non-null `UavStatus`; permitted state transitions are not yet modeled.

## Consequences

- Mission Control can begin tracking a UAV without first recording a false ground state.
- Every internal caller must provide the best currently known status.
- A future external registration payload must include both current position and current status.
- The external adapter will need to define authority, validation, stale-data handling, acknowledgements, and protocol errors.
- Registration status remains a report from the caller rather than proof of the aircraft's physical condition.

## Alternatives Considered

### Default to `GROUND`

Rejected because registration may happen after takeoff or during another operational state.

### Default to `UNKNOWN`

Not selected. It avoids a false physical claim but still permits registration without the state required by the current model. An unknown state can be introduced later if external protocol requirements justify it.

### Infer Status from Altitude

Rejected because altitude does not express operational intent. The same altitude may be compatible with takeoff, landing, hovering, flight, or a stale report.

### Keep a Convenience Constructor

Rejected because an overload with an implicit status would preserve inconsistent registration behavior.

## Out of Scope

This decision does not define:

- Valid transitions between UAV statuses.
- Command handling or command-to-state relationships.
- STOMP destinations, message DTOs, or handlers.
- Authentication, authorization, or status authority.
- Telemetry timestamps, stale-state detection, or reconnection reconciliation.
- Durable persistence or state restoration.

These concerns remain part of the project's future development rather than this registration decision.
