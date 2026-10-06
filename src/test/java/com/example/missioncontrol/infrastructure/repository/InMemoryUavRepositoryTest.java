package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.Position;
import com.example.missioncontrol.domain.model.Uav;
import com.example.missioncontrol.domain.model.UavStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryUavRepositoryTest {

    private InMemoryUavRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUavRepository();
    }

    @Test
    void returnsEmptyResultsInitially() {
        assertTrue(repository.findById(UUID.randomUUID()).isEmpty());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void savesAndFindsUav() {
        Uav uav = uav(UUID.randomUUID(), UavStatus.GROUND);

        assertSame(uav, repository.save(uav));
        assertSame(uav, repository.findById(uav.getId()).orElseThrow());
        assertEquals(List.of(uav), repository.findAll());
    }

    @Test
    void replacesUavWithTheSameId() {
        UUID id = UUID.randomUUID();
        Uav first = uav(id, UavStatus.GROUND);
        Uav replacement = uav(id, UavStatus.FLYING);

        repository.save(first);
        repository.save(replacement);

        assertSame(replacement, repository.findById(id).orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findAllReturnsAnUnmodifiableSnapshot() {
        Uav first = uav(UUID.randomUUID(), UavStatus.GROUND);
        repository.save(first);
        List<Uav> snapshot = repository.findAll();

        repository.save(uav(UUID.randomUUID(), UavStatus.FLYING));

        assertEquals(List.of(first), snapshot);
        assertEquals(2, repository.findAll().size());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(uav(UUID.randomUUID(), UavStatus.GROUND)));
    }

    @Test
    void rejectsNullInputs() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findById(null));
    }

    private Uav uav(UUID id, UavStatus status) {
        return new Uav(id, new Position(42.28, -8.73, 0), status);
    }
}
