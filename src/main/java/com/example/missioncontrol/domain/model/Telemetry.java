package com.example.missioncontrol.domain.model;

import java.time.Instant;

public record Telemetry(
        Uav uav,
        Position position,
        UavStatus status,
        Byte battery,
        Instant lastUpdate
) {
}
