package com.example.missioncontrol.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UavTest {

    @Test
    void preservesConstructorArguments() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Position position = new Position(42.28, -8.73, 20);

        Uav uav = new Uav(
                id,
                position,
                UavStatus.FLYING
        );

        assertEquals(id, uav.getId());
        assertSame(position, uav.getPosition());
        assertEquals(UavStatus.FLYING, uav.getStatus());
    }

    @Test
    void updatesPosition() {
        Uav uav = new Uav(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
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

    @Test
    void rejectsNullConstructorArguments() {
        UUID id = UUID.randomUUID();
        Position position = new Position(42.28, -8.73, 0);

        assertThrows(NullPointerException.class,
                () -> new Uav(null, position, UavStatus.GROUND));
        assertThrows(NullPointerException.class,
                () -> new Uav(id, null, UavStatus.GROUND));
        assertThrows(NullPointerException.class,
                () -> new Uav(id, position, null));
    }

    @Test
    void rejectsNullUpdatesWithoutChangingState() {
        Position initialPosition = new Position(42.28, -8.73, 0);
        Uav uav = new Uav(UUID.randomUUID(), initialPosition, UavStatus.GROUND);

        assertThrows(NullPointerException.class, () -> uav.updatePosition(null));
        assertThrows(NullPointerException.class, () -> uav.updateStatus(null));
        assertSame(initialPosition, uav.getPosition());
        assertEquals(UavStatus.GROUND, uav.getStatus());
    }
}
