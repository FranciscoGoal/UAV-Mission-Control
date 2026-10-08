package com.example.missioncontrol.infrastructure.controller;

import com.example.missioncontrol.application.service.UavService;
import com.example.missioncontrol.domain.model.Position;
import com.example.missioncontrol.domain.model.Telemetry;
import com.example.missioncontrol.domain.model.UavStatus;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelemetryControllerTest {

    private final UavService uavService = mock(UavService.class);
    private final TelemetryController controller = new TelemetryController(uavService);

    @Test
    void forwardsTelemetryForAuthenticatedUav() {
        UUID uavId = UUID.randomUUID();
        Position position = new Position(42.29, -8.74, 50);
        Telemetry telemetry = new Telemetry(
                uavId,
                position,
                UavStatus.FLYING,
                (byte) 75,
                Instant.parse("2026-10-09T00:30:00Z")
        );
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(uavId.toString());

        controller.receiveTelemetry(telemetry, principal);

        verify(uavService).updateTelemetry(uavId, telemetry);
    }
}
