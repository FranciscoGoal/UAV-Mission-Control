package com.example.missioncontrol.domain.model.repository;

import com.example.missioncontrol.domain.model.command.CommandExecution;
import com.example.missioncontrol.domain.model.command.CommandStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommandExecutionRepository {

    CommandExecution save(CommandExecution execution);

    Optional<CommandExecution> findByCommandId(UUID commandId);

    List<CommandExecution> findByUavId(String uavId);

    List<CommandExecution> findByStatus(CommandStatus status);

    List<CommandExecution> findAll();
}
