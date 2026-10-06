package com.example.missioncontrol.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Uav {

    private final UUID id;
    private Position position;
    private UavStatus status;

    public Uav(UUID id, Position position) {
        this.id = Objects.requireNonNull(id, "UAV id cannot be null");
        this.position = Objects.requireNonNull(position, "Position cannot be null");
        this.status = UavStatus.GROUND;
    }

    public void updatePosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position cannot be null");
    }

    public void updateStatus(UavStatus status) {
        this.status = Objects.requireNonNull(status, "UAV status cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public UavStatus getStatus() {
        return status;
    }
}
