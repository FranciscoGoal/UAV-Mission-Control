package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.command.CommandExecution;
import com.example.missioncontrol.domain.model.command.CommandStatus;
import com.example.missioncontrol.domain.model.command.LandCommand;
import com.example.missioncontrol.domain.model.command.ReturnHomeCommand;
import com.example.missioncontrol.domain.model.command.UavCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCommandExecutionRepositoryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-06T18:00:00Z");

    private InMemoryCommandExecutionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCommandExecutionRepository();
    }

    @Test
    void returnsEmptyResultsInitially() {
        assertTrue(repository.findByCommandId(UUID.randomUUID()).isEmpty());
        assertTrue(repository.findByUavId("uav-1").isEmpty());
        assertTrue(repository.findByStatus(CommandStatus.PENDING).isEmpty());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void savesAndFindsExecutionByCommandId() {
        CommandExecution execution = execution(
                command(id(1), "uav-1", CREATED_AT),
                CommandStatus.PENDING,
                CREATED_AT
        );

        assertSame(execution, repository.save(execution));
        assertSame(
                execution,
                repository.findByCommandId(execution.command().id()).orElseThrow()
        );
    }

    @Test
    void replacesCurrentExecutionForTheSameCommand() {
        UavCommand command = command(id(1), "uav-1", CREATED_AT);
        CommandExecution pending = execution(command, CommandStatus.PENDING, CREATED_AT);
        CommandExecution sent = execution(
                command, CommandStatus.SENT, CREATED_AT.plusSeconds(1)
        );

        repository.save(pending);
        repository.save(sent);

        assertSame(sent, repository.findByCommandId(command.id()).orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void acceptsIdempotentUpdateAtTheSameTime() {
        UavCommand command = command(id(1), "uav-1", CREATED_AT);
        CommandExecution first = execution(command, CommandStatus.PENDING, CREATED_AT);
        CommandExecution replacement = execution(command, CommandStatus.SENT, CREATED_AT);

        repository.save(first);

        assertSame(replacement, repository.save(replacement));
        assertSame(replacement, repository.findByCommandId(command.id()).orElseThrow());
    }

    @Test
    void rejectsOlderUpdateAndKeepsCurrentExecution() {
        UavCommand command = command(id(1), "uav-1", CREATED_AT);
        CommandExecution current = execution(
                command, CommandStatus.SENT, CREATED_AT.plusSeconds(2)
        );
        CommandExecution stale = execution(
                command, CommandStatus.PENDING, CREATED_AT.plusSeconds(1)
        );
        repository.save(current);

        assertThrows(IllegalArgumentException.class, () -> repository.save(stale));
        assertSame(current, repository.findByCommandId(command.id()).orElseThrow());
    }

    @Test
    void rejectsAChangedCommandDefinitionForTheSameId() {
        UUID commandId = id(1);
        UavCommand originalCommand = command(commandId, "uav-1", CREATED_AT);
        UavCommand changedCommand = new ReturnHomeCommand(
                commandId, "uav-2", CREATED_AT, CREATED_AT.plusSeconds(10)
        );
        CommandExecution original = execution(
                originalCommand, CommandStatus.PENDING, CREATED_AT
        );
        CommandExecution inconsistent = execution(
                changedCommand, CommandStatus.SENT, CREATED_AT.plusSeconds(1)
        );
        repository.save(original);

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(inconsistent)
        );
        assertSame(original, repository.findByCommandId(commandId).orElseThrow());
    }

    @Test
    void filtersAndOrdersExecutionsByUpdateTimeThenId() {
        CommandExecution latestPending = execution(
                command(id(3), "uav-1", CREATED_AT),
                CommandStatus.PENDING,
                CREATED_AT.plusSeconds(2)
        );
        CommandExecution firstPending = execution(
                command(id(1), "uav-1", CREATED_AT),
                CommandStatus.PENDING,
                CREATED_AT
        );
        CommandExecution secondPending = execution(
                command(id(2), "uav-1", CREATED_AT),
                CommandStatus.PENDING,
                CREATED_AT
        );
        CommandExecution other = execution(
                command(id(4), "uav-2", CREATED_AT),
                CommandStatus.SENT,
                CREATED_AT.minusSeconds(1)
        );

        repository.save(latestPending);
        repository.save(secondPending);
        repository.save(other);
        repository.save(firstPending);

        assertEquals(
                List.of(firstPending, secondPending, latestPending),
                repository.findByUavId("uav-1")
        );
        assertEquals(
                List.of(firstPending, secondPending, latestPending),
                repository.findByStatus(CommandStatus.PENDING)
        );
        assertEquals(
                List.of(other, firstPending, secondPending, latestPending),
                repository.findAll()
        );
    }

    @Test
    void listingsAreUnmodifiableSnapshots() {
        CommandExecution first = execution(
                command(id(1), "uav-1", CREATED_AT),
                CommandStatus.PENDING,
                CREATED_AT
        );
        repository.save(first);
        List<CommandExecution> snapshot = repository.findAll();

        repository.save(execution(
                command(id(2), "uav-1", CREATED_AT),
                CommandStatus.PENDING,
                CREATED_AT.plusSeconds(1)
        ));

        assertEquals(List.of(first), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(first));
    }

    @Test
    void concurrentUpdatesKeepTheNewestExecution() throws Exception {
        UavCommand command = command(id(1), "uav-1", CREATED_AT);
        CommandExecution initial = execution(command, CommandStatus.PENDING, CREATED_AT);
        CommandExecution older = execution(
                command, CommandStatus.SENT, CREATED_AT.plusSeconds(1)
        );
        CommandExecution newest = execution(
                command, CommandStatus.ACKNOWLEDGED, CREATED_AT.plusSeconds(2)
        );
        repository.save(initial);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> olderSave = executor.submit(() -> saveAfterSignal(start, older));
            Future<?> newestSave = executor.submit(() -> saveAfterSignal(start, newest));
            start.countDown();
            olderSave.get();
            newestSave.get();
        }

        assertSame(newest, repository.findByCommandId(command.id()).orElseThrow());
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findByCommandId(null));
        assertThrows(NullPointerException.class, () -> repository.findByStatus(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void rejectsBlankUavId(String uavId) {
        assertThrows(IllegalArgumentException.class, () -> repository.findByUavId(uavId));
    }

    @Test
    void rejectsNullUavId() {
        assertThrows(IllegalArgumentException.class, () -> repository.findByUavId(null));
    }

    private void saveAfterSignal(CountDownLatch start, CommandExecution execution) {
        try {
            start.await();
            repository.save(execution);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        } catch (IllegalArgumentException ignored) {
            // The older concurrent update is expected to lose if the newest saves first.
        }
    }

    private CommandExecution execution(
            UavCommand command,
            CommandStatus status,
            Instant updatedAt
    ) {
        return new CommandExecution(
                command, status, "operator-1", List.of(), updatedAt
        );
    }

    private LandCommand command(UUID commandId, String uavId, Instant createdAt) {
        return new LandCommand(commandId, uavId, createdAt, createdAt.plusSeconds(10));
    }

    private UUID id(int suffix) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(suffix));
    }
}
