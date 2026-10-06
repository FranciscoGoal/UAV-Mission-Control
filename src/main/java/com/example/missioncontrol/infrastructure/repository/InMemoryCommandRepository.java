package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.command.UavCommand;
import com.example.missioncontrol.domain.model.repository.CommandRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryCommandRepository implements CommandRepository {

    private static final Comparator<UavCommand> COMMAND_ORDER = Comparator
            .comparing(UavCommand::createdAt)
            .thenComparing(UavCommand::id);

    private final ConcurrentMap<UUID, UavCommand> commands = new ConcurrentHashMap<>();

    @Override
    public UavCommand save(UavCommand command) {
        Objects.requireNonNull(command, "Command cannot be null");
        commands.put(command.id(), command);
        return command;
    }

    @Override
    public Optional<UavCommand> findById(UUID id) {
        Objects.requireNonNull(id, "Command id cannot be null");
        return Optional.ofNullable(commands.get(id));
    }

    @Override
    public List<UavCommand> findByUavId(String uavId) {
        validateUavId(uavId);
        return commands.values().stream()
                .filter(command -> command.uavId().equals(uavId))
                .sorted(COMMAND_ORDER)
                .toList();
    }

    @Override
    public List<UavCommand> findAll() {
        return commands.values().stream()
                .sorted(COMMAND_ORDER)
                .toList();
    }

    private static void validateUavId(String uavId) {
        if (uavId == null || uavId.isBlank()) {
            throw new IllegalArgumentException("UAV id cannot be empty");
        }
    }
}
