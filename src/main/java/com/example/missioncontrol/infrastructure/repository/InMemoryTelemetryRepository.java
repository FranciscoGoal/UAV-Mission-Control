package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.Telemetry;
import com.example.missioncontrol.domain.model.repository.TelemetryRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryTelemetryRepository implements TelemetryRepository {

    private final ConcurrentMap<UUID, List<Telemetry>> telemetryByUav =
            new ConcurrentHashMap<>();

    @Override
    public Telemetry save(Telemetry telemetry) {
        Objects.requireNonNull(telemetry, "Telemetry cannot be null");

        telemetryByUav.compute(telemetry.uavId(), (uavId, history) -> {
            if (history != null) {
                Telemetry latest = history.getLast();
                if (!telemetry.lastUpdate().isAfter(latest.lastUpdate())) {
                    throw new IllegalArgumentException(
                            "Telemetry must be newer than the stored value"
                    );
                }
            }

            List<Telemetry> updatedHistory = history == null
                    ? new ArrayList<>()
                    : new ArrayList<>(history);
            updatedHistory.add(telemetry);
            return List.copyOf(updatedHistory);
        });

        return telemetry;
    }

    @Override
    public Optional<Telemetry> findLatestByUavId(UUID uavId) {
        Objects.requireNonNull(uavId, "UAV id cannot be null");
        List<Telemetry> history = telemetryByUav.get(uavId);
        return history == null || history.isEmpty()
                ? Optional.empty()
                : Optional.of(history.getLast());
    }

    @Override
    public List<Telemetry> findByUavId(UUID uavId) {
        Objects.requireNonNull(uavId, "UAV id cannot be null");
        return telemetryByUav.getOrDefault(uavId, List.of());
    }
}
