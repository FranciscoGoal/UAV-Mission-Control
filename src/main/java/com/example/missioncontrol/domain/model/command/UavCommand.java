package com.example.missioncontrol.domain.model.command;

import java.time.Instant;
import java.util.UUID;

public interface UavCommand {

    UUID    id();
    String  uavId();
    CommandType type();
    Instant createdAt();
    Instant expiresAt();
}
