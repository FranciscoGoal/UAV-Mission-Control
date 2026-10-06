package com.example.missioncontrol.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UavTest {

    @Test
    void startsOnGround() {
        Uav uav = new Uav(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                new Position(42.28, -8.73, 0),
                UavStatus.GROUND
        );

        assertEquals(UavStatus.GROUND, uav.getStatus());
    }

    @Test
    void updatesPosition() {
        Uav uav = new Uav(
                UUID.fromString("00000000-0000-0000-000-000000000001"),
                new Position(42.28, -8.73, 0),
                UavStatus.FLYING
        );
        Position newPosition = new Position(42.29, -8.74, 20);

        uav.updatePosition(newPosition);

        assertEquals(newPosition, uav.getPosition());
    }

    @Test
    void updatesStatus() {
        Uav uav = new Uav(
                UUID.fromString("00000000-0000-0000-000-000000000001"),
                new Position(42.29, -8.74, 20),
                UavStatus.GROUND
        );

        UavStatus newStatus = UavStatus.TAKING_OFF;
        uav.updateStatus(newStatus);

        assertEquals(newStatus, uav.getStatus());
    }
}
