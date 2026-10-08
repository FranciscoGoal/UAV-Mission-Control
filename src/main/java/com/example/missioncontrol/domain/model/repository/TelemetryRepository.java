package com.example.missioncontrol.domain.model.repository;

import com.example.missioncontrol.domain.model.Telemetry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryRepository {

    Telemetry save(Telemetry telemetry);

    Optional<Telemetry> findLatestByUavId(UUID uavId);

    List<Telemetry> findByUavId(UUID uavId);
}
