package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.Position;
import com.example.missioncontrol.domain.model.command.GoToCommand;
import com.example.missioncontrol.domain.model.command.LandCommand;
import com.example.missioncontrol.domain.model.command.ReturnHomeCommand;
import com.example.missioncontrol.domain.model.command.TakeOffCommand;
import com.example.missioncontrol.domain.model.command.UavCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCommandRepositoryTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-06T18:00:00Z");

    private InMemoryCommandRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCommandRepository();
    }

    @Test
    void returnsEmptyResultsInitially() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
        assertTrue(repository.findByUavId("uav-1").isEmpty());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void savesAndFindsEveryCommandType() {
        List<UavCommand> commands = List.of(
                takeOff(id(1), "uav-1", CREATED_AT),
                goTo(id(2), "uav-1", CREATED_AT.plusSeconds(1)),
                land(id(3), "uav-1", CREATED_AT.plusSeconds(2)),
                returnHome(id(4), "uav-1", CREATED_AT.plusSeconds(3))
        );

        for (UavCommand command : commands) {
            assertSame(command, repository.save(command));
            assertSame(command, repository.findById(command.id()).orElseThrow());
        }

        assertEquals(commands, repository.findAll());
    }

    @Test
    void replacesCommandWithTheSameId() {
        UUID commandId = id(1);
        UavCommand first = land(commandId, "uav-1", CREATED_AT);
        UavCommand replacement = returnHome(commandId, "uav-2", CREATED_AT.plusSeconds(1));

        repository.save(first);
        repository.save(replacement);

        assertSame(replacement, repository.findById(commandId).orElseThrow());
        assertEquals(List.of(replacement), repository.findAll());
        assertTrue(repository.findByUavId("uav-1").isEmpty());
    }

    @Test
    void filtersByUavAndOrdersByCreationThenId() {
        UavCommand latest = land(id(3), "uav-1", CREATED_AT.plusSeconds(2));
        UavCommand firstById = takeOff(id(1), "uav-1", CREATED_AT);
        UavCommand secondById = goTo(id(2), "uav-1", CREATED_AT);
        UavCommand otherUav = returnHome(id(4), "uav-2", CREATED_AT.minusSeconds(1));

        repository.save(latest);
        repository.save(secondById);
        repository.save(otherUav);
        repository.save(firstById);

        assertEquals(
                List.of(firstById, secondById, latest),
                repository.findByUavId("uav-1")
        );
        assertEquals(
                List.of(otherUav, firstById, secondById, latest),
                repository.findAll()
        );
    }

    @Test
    void listingsAreUnmodifiableSnapshots() {
        UavCommand first = land(id(1), "uav-1", CREATED_AT);
        repository.save(first);
        List<UavCommand> snapshot = repository.findAll();

        repository.save(returnHome(id(2), "uav-1", CREATED_AT.plusSeconds(1)));

        assertEquals(List.of(first), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(first));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
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

    private TakeOffCommand takeOff(UUID commandId, String uavId, Instant createdAt) {
        return new TakeOffCommand(
                commandId, uavId, 25, createdAt, createdAt.plusSeconds(10)
        );
    }

    private GoToCommand goTo(UUID commandId, String uavId, Instant createdAt) {
        return new GoToCommand(
                commandId,
                uavId,
                new Position(42.29, -8.74, 50),
                createdAt,
                createdAt.plusSeconds(10)
        );
    }

    private LandCommand land(UUID commandId, String uavId, Instant createdAt) {
        return new LandCommand(commandId, uavId, createdAt, createdAt.plusSeconds(10));
    }

    private ReturnHomeCommand returnHome(UUID commandId, String uavId, Instant createdAt) {
        return new ReturnHomeCommand(commandId, uavId, createdAt, createdAt.plusSeconds(10));
    }

    private UUID id(int suffix) {
        return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(suffix));
    }
}
