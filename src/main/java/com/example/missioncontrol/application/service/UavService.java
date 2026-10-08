package com.example.missioncontrol.application.service;

import com.example.missioncontrol.domain.model.Position;
import com.example.missioncontrol.domain.model.Telemetry;
import com.example.missioncontrol.domain.model.Uav;
import com.example.missioncontrol.domain.model.UavStatus;
import com.example.missioncontrol.domain.model.repository.TelemetryRepository;
import com.example.missioncontrol.domain.model.repository.UavRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class UavService {

    private final UavRepository uavRepository;
    private final TelemetryRepository telemetryRepository;

    public UavService(
            UavRepository uavRepository,
            TelemetryRepository telemetryRepository
    ) {
        this.uavRepository = uavRepository;
        this.telemetryRepository = telemetryRepository;
    }

    public synchronized Uav registerUav(
                            UUID id,
                            Position initialPosition,
                            UavStatus currentStatus
    ) {

        if (uavRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException(
                    "A UAV with this id already exists: " + id
            );
        }

        Uav uav = new Uav(id, initialPosition, currentStatus);

        return uavRepository.save(uav);

    }

    public synchronized Uav getUav(UUID id) {
        return uavRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UAV not registered: " + id));
    }

    public synchronized Uav updatePosition(UUID id, Position newPosition) {
        Uav uav = uavRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UAV not registered: " + id));

        uav.updatePosition(newPosition);

        return uavRepository.save(uav);
    }

    public synchronized Uav updateStatus(UUID id, UavStatus newStatus) {
        Uav uav = uavRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UAV not registered: " + id));

        uav.updateStatus(newStatus);

        return uavRepository.save(uav);
    }

    public synchronized Uav updateTelemetry(UUID authenticatedUavId, Telemetry telemetry) {
        Objects.requireNonNull(authenticatedUavId, "Authenticated UAV id cannot be null");
        Objects.requireNonNull(telemetry, "Telemetry cannot be null");

        if (!authenticatedUavId.equals(telemetry.uavId())) {
            throw new IllegalArgumentException(
                    "Telemetry UAV id does not match the authenticated UAV"
            );
        }

        Uav uav = uavRepository.findById(authenticatedUavId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "UAV not registered: " + authenticatedUavId
                ));

        telemetryRepository.save(telemetry);
        uav.updatePosition(telemetry.position());
        uav.updateStatus(telemetry.status());

        return uavRepository.save(uav);
    }
}
