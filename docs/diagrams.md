# Mission Control Technical Diagrams

This gallery documents the behavior implemented in the repository today. Every diagram is a committed static SVG for consistent GitHub rendering; editable Mermaid sources live in [`diagram-sources/`](diagram-sources/).

## System Architecture

The STOMP transport feeds validated telemetry snapshots into the UAV application flow through `TelemetryController`. `UavService` updates current UAV state and retains chronological telemetry history. `CommandService` also connects stored commands to initial `PENDING` execution snapshots, while dispatch and result handling remain outside the current boundary.

![Mission Control system architecture](assets/diagrams/system-architecture.svg)

All four repositories use concurrent in-memory maps. Their contents disappear when the process stops, and there is no transactional relationship among them.

## Command Interaction

The code can construct and store commands through direct Java calls. `CommandService` looks up a stored command and creates its initial `PENDING` execution snapshot. It does not yet receive commands through transport, validate the target against a registered UAV, dispatch commands, process acknowledgements, or publish results.

![Current command interaction](assets/diagrams/command-interaction.svg)

An execution repository update is accepted when it embeds the same command and its timestamp is not older than the stored snapshot. Status-to-status transitions are not evaluated.

## Telemetry Update Flow

STOMP clients can send a complete `Telemetry` payload to `/app/telemetry`. `TelemetryController` derives the authenticated UAV UUID from the connection principal, while `UavService` requires it to match the payload's `uavId`.

![Current position update flow](assets/diagrams/telemetry-flow.svg)

`Telemetry` validates its identity, position, status, battery percentage, and timestamp. The service rejects unknown UAVs and the repository rejects duplicate or out-of-order samples. Accepted samples update the UAV's current position and status and remain available in an in-memory chronological history. Authentication, outbound publication, and protocol-level errors are not implemented.

## UAV Status Behavior

The status enum provides operational vocabulary but not a controlled flight-state machine. Registration accepts any declared non-null status, and any declared status can replace any other.

![UAV status behavior](assets/diagrams/uav-status.svg)

The implementation also permits self-transitions. A null update is rejected, and position or altitude does not constrain the selected status.

## Command Status Behavior

Command statuses describe possible snapshots, not an enforced lifecycle. Callers can create an execution with any declared status and replace it with any other status when repository consistency checks pass.

![Command status behavior](assets/diagrams/command-status.svg)

There are no terminal-state guards, automatic expiration, retries, timeout processing, or execution history.

## UAV Domain

![UAV domain model](assets/diagrams/uav-domain.svg)

`Uav` owns its latest immutable `Position` value and current `UavStatus`. `UavService` coordinates registration, lookup, direct updates, and complete telemetry updates through the UAV and telemetry repository ports.

## Command Domain

![Command domain model](assets/diagrams/command-domain.svg)

The relationship between a command and a UAV is conceptual rather than enforced: commands carry a non-blank string identifier while `Uav` uses `UUID`. No service currently resolves that reference or verifies that the target exists. `CommandExecution` embeds a command and has no separate execution identifier or result payload.

## Views Not Included

Mission lifecycle, simulation, operator-interface, and database diagrams are intentionally absent because those subsystems have not been implemented. They belong to the roadmap and should be added when their contracts and behavior exist in code.
