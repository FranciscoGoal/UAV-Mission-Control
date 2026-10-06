package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.command.CommandExecution;
import com.example.missioncontrol.domain.model.command.CommandStatus;
import com.example.missioncontrol.domain.model.repository.CommandExecutionRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryCommandExecutionRepository implements CommandExecutionRepository {

    private static final Comparator<CommandExecution> EXECUTION_ORDER = Comparator
            .comparing(CommandExecution::updatedAt)
            .thenComparing(execution -> execution.command().id());

    private final ConcurrentMap<UUID, CommandExecution> executions = new ConcurrentHashMap<>();

    @Override
    public CommandExecution save(CommandExecution execution) {
        Objects.requireNonNull(execution, "Command execution cannot be null");
        UUID commandId = execution.command().id();

        return executions.compute(commandId, (id, current) -> {
            if (current == null) {
                return execution;
            }
            if (!current.command().equals(execution.command())) {
                throw new IllegalArgumentException(
                        "Command definition cannot change for id: " + commandId
                );
            }
            if (execution.updatedAt().isBefore(current.updatedAt())) {
                throw new IllegalArgumentException(
                        "Execution update cannot be older than the stored value"
                );
            }
            return execution;
        });
    }

    @Override
    public Optional<CommandExecution> findByCommandId(UUID commandId) {
        Objects.requireNonNull(commandId, "Command id cannot be null");
        return Optional.ofNullable(executions.get(commandId));
    }

    @Override
    public List<CommandExecution> findByUavId(String uavId) {
        validateUavId(uavId);
        return executions.values().stream()
                .filter(execution -> execution.command().uavId().equals(uavId))
                .sorted(EXECUTION_ORDER)
                .toList();
    }

    @Override
    public List<CommandExecution> findByStatus(CommandStatus status) {
        Objects.requireNonNull(status, "Command status cannot be null");
        return executions.values().stream()
                .filter(execution -> execution.status() == status)
                .sorted(EXECUTION_ORDER)
                .toList();
    }

    @Override
    public List<CommandExecution> findAll() {
        return executions.values().stream()
                .sorted(EXECUTION_ORDER)
                .toList();
    }

    private static void validateUavId(String uavId) {
        if (uavId == null || uavId.isBlank()) {
            throw new IllegalArgumentException("UAV id cannot be empty");
        }
    }
}
