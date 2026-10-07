package com.example.missioncontrol.infrastructure.controller;

import com.example.missioncontrol.application.service.UavService;
import com.example.missioncontrol.domain.model.Position;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class TelemetryController {

    private final UavService uavService;

    public TelemetryController(UavService uavService) {
        this.uavService = uavService;
    }

    @MessageMapping("/telemetry")
    public void receiveTelemetry(
            Position position,
            Principal uav
    ) {
        uavService.updatePosition(
                    UUID.fromString(uav.getName()),
                    position
        );
    }
}
