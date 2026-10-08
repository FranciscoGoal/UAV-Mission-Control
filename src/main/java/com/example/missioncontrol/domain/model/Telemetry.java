package com.example.missioncontrol.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Telemetry(
        UUID uavId,
        Position position,
        UavStatus status,
        Byte battery,
        Instant lastUpdate
) {
    public Telemetry {
        Objects.requireNonNull(uavId, "UAV id cannot be null");
        Objects.requireNonNull(position, "Position cannot be null");
        Objects.requireNonNull(status, "UAV status cannot be null");
        Objects.requireNonNull(battery, "Battery cannot be null");
        Objects.requireNonNull(lastUpdate, "Last update cannot be null");

        if (battery < 0 || battery > 100) {
            throw new IllegalArgumentException("Battery must be between 0 and 100");
        }
    }
}
