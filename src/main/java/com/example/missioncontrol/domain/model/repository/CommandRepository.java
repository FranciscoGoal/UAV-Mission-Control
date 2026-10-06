package com.example.missioncontrol.domain.model.repository;

import com.example.missioncontrol.domain.model.command.UavCommand;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommandRepository {

    UavCommand save(UavCommand command);

    Optional<UavCommand> findById(UUID id);

    List<UavCommand> findByUavId(String uavId);

    List<UavCommand> findAll();
}
