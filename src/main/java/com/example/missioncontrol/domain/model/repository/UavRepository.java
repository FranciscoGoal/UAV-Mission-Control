package com.example.missioncontrol.domain.model.repository;

import com.example.missioncontrol.domain.model.Uav;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UavRepository {

    Uav save(Uav uav);

    Optional<Uav> findById(UUID id);

    List<Uav> findAll();
}
