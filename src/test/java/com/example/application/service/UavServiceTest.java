package com.example.missioncontrol.application.service;

import com.example.missioncontrol.domain.model.Position;
import com.example.missioncontrol.domain.model.Uav;
import com.example.missioncontrol.domain.model.UavStatus;
import com.example.missioncontrol.infrastructure.repository.InMemoryUavRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UavServiceTest {

    @Test
    void updatePosition() {
        Uav uav = new Uav(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                new Position(42.28, -8.73, 0),
                UavStatus.GROUND
        );
        Position newPosition = new Position(42.29, -8.74, 20);

        InMemoryUavRepository repository = new InMemoryUavRepository();
        repository.save(uav);
        UavService service = new UavService(repository);

        service.updatePosition(uav.getId(), newPosition);

        assertEquals(newPosition, uav.getPosition());
    }
}
