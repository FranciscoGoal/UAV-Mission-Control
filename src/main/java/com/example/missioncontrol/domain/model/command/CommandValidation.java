package com.example.missioncontrol.domain.model.command;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

final class CommandValidation {

    private CommandValidation() {
    }

    static void validate(
            UUID id,
            String uavId,
            Instant createdAt,
            Instant expiresAt
    ) {
        Objects.requireNonNull(id, "Command id cannot be null");
        Objects.requireNonNull(createdAt, "Creation time cannot be null");
        Objects.requireNonNull(expiresAt, "Expiration time cannot be null");

        if (uavId == null || uavId.isBlank()) {
            throw new IllegalArgumentException("UAV id cannot be empty");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "Expiration time must be after creation time"
            );
        }
    }
}
