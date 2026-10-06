package com.example.missioncontrol.domain.model.command;

import java.time.Instant;
import java.util.UUID;


public record TakeOffCommand (
    
        UUID id,
        String uavId,
        double targetAltitudeMeters,
        Instant createdAt,
        Instant expiresAt

) implements UavCommand {
    
    private static final long EXPIRATION_SECONDS = 10;
    
    public TakeOffCommand {
        CommandValidation.validate(id, uavId, createdAt, expiresAt);
        if (!Double.isFinite(targetAltitudeMeters) || targetAltitudeMeters <= 0) {
            throw new IllegalArgumentException(
                    "Target altitude must be finite and greater than zero"
            );
        }
    }

    public static TakeOffCommand create(
            String uavId,
            double targetAltitudeMeters
    ) {
        
        Instant now = Instant.now();
        
        return new TakeOffCommand(
                UUID.randomUUID(),
                uavId,
                targetAltitudeMeters,
                now,
                now.plusSeconds(EXPIRATION_SECONDS)
        );

    }

    @Override
    public CommandType type() {
        return CommandType.TAKE_OFF;
    }

}
