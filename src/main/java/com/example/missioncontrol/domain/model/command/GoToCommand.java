package com.example.missioncontrol.domain.model.command;

import com.example.missioncontrol.domain.model.Position;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record GoToCommand(
        UUID id,
        String uavId,
        Position targetPosition,
        Instant createdAt,
        Instant expiresAt
) implements UavCommand {

    private static final long EXPIRATION_SECONDS = 10;

    public GoToCommand {
        CommandValidation.validate(id, uavId, createdAt, expiresAt);
        Objects.requireNonNull(targetPosition, "Target position cannot be null");
    }

    public static GoToCommand create(String uavId, Position targetPosition) {
        Instant now = Instant.now();

        return new GoToCommand(
                UUID.randomUUID(),
                uavId,
                targetPosition,
                now,
                now.plusSeconds(EXPIRATION_SECONDS)
        );
    }

    @Override
    public CommandType type() {
        return CommandType.GO_TO;
    }
}
