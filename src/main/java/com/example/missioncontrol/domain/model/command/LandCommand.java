package com.example.missioncontrol.domain.model.command;

import java.time.Instant;
import java.util.UUID;

public record LandCommand(
        UUID id,
        String uavId,
        Instant createdAt,
        Instant expiresAt
) implements UavCommand {

    private static final long EXPIRATION_SECONDS = 10;

    public LandCommand {
        CommandValidation.validate(id, uavId, createdAt, expiresAt);
    }

    public static LandCommand create(String uavId) {
        Instant now = Instant.now();

        return new LandCommand(
                UUID.randomUUID(),
                uavId,
                now,
                now.plusSeconds(EXPIRATION_SECONDS)
        );
    }

    @Override
    public CommandType type() {
        return CommandType.LAND;
    }
}
