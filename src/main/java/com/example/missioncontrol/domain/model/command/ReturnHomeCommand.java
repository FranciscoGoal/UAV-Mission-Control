package com.example.missioncontrol.domain.model.command;

import java.time.Instant;
import java.util.UUID;

public record ReturnHomeCommand(
        UUID id,
        String uavId,
        Instant createdAt,
        Instant expiresAt
) implements UavCommand {

    private static final long EXPIRATION_SECONDS = 10;

    public ReturnHomeCommand {
        CommandValidation.validate(id, uavId, createdAt, expiresAt);
    }

    public static ReturnHomeCommand create(String uavId) {
        Instant now = Instant.now();

        return new ReturnHomeCommand(
                UUID.randomUUID(),
                uavId,
                now,
                now.plusSeconds(EXPIRATION_SECONDS)
        );
    }

    @Override
    public CommandType type() {
        return CommandType.RETURN_HOME;
    }
}
