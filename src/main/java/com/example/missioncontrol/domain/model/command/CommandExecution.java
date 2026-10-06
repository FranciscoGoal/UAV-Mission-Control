package com.example.missioncontrol.domain.model.command;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record CommandExecution (

    UavCommand command,
    CommandStatus status,
    String operatorSesionId,
    List<String> parameters,
    Instant updatedAt
) {

    public CommandExecution {
        Objects.requireNonNull(command, "Command cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(operatorSesionId, "Operator sesion ID cannot be null");
        parameters = List.copyOf(
                Objects.requireNonNull(parameters, "Parameters cannot be null")
        );  // Autorize empty list but not null ones.
        Objects.requireNonNull(updatedAt, "Update time cannot be null");
    }
}
