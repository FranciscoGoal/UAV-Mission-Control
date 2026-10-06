package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.Uav;
import com.example.missioncontrol.domain.model.repository.UavRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryUavRepository implements UavRepository {

    private final ConcurrentMap<UUID, Uav> uavs = new ConcurrentHashMap<>();

    @Override
    public Uav save(Uav uav) {
        uavs.put(uav.getId(), uav);
        return uav;
    }

    @Override
    public Optional<Uav> findById(UUID id) {
        return Optional.ofNullable(uavs.get(id));
    }

    @Override
    public List<Uav> findAll() {
        return List.copyOf(uavs.values());
    }
}
