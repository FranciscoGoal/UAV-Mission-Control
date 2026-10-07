package com.example.missioncontrol.infrastructure.controller;

import com.example.missioncontrol.application.service.UavService;
import com.example.missioncontrol.domain.model.Position;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelemetryControllerTest {

    private final UavService uavService = mock(UavService.class);
    private final TelemetryController controller = new TelemetryController(uavService);

    @Test
    void updatesPositionForAuthenticatedUav() {
        UUID uavId = UUID.randomUUID();
        Position position = new Position(42.29, -8.74, 50);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(uavId.toString());

        controller.receiveTelemetry(position, principal);

        verify(uavService).updatePosition(uavId, position);
    }
}
