package com.example.missioncontrol.infrastructure.repository;

import com.example.missioncontrol.domain.model.Position;
import com.example.missioncontrol.domain.model.Telemetry;
import com.example.missioncontrol.domain.model.UavStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTelemetryRepositoryTest {

    private static final UUID UAV_ID = UUID.randomUUID();
    private static final Instant FIRST_UPDATE = Instant.parse("2026-10-09T00:30:00Z");

    private InMemoryTelemetryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTelemetryRepository();
    }

    @Test
    void returnsEmptyResultsInitially() {
        assertTrue(repository.findByUavId(UAV_ID).isEmpty());
        assertTrue(repository.findLatestByUavId(UAV_ID).isEmpty());
    }

    @Test
    void preservesHistoryAndReturnsLatestTelemetry() {
        Telemetry first = telemetry(FIRST_UPDATE);
        Telemetry second = telemetry(FIRST_UPDATE.plusSeconds(1));

        assertSame(first, repository.save(first));
        assertSame(second, repository.save(second));

        assertEquals(List.of(first, second), repository.findByUavId(UAV_ID));
        assertSame(second, repository.findLatestByUavId(UAV_ID).orElseThrow());
    }

    @Test
    void rejectsOlderOrDuplicateTelemetryWithoutChangingHistory() {
        Telemetry current = telemetry(FIRST_UPDATE.plusSeconds(1));
        repository.save(current);

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(telemetry(FIRST_UPDATE))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(telemetry(FIRST_UPDATE.plusSeconds(1)))
        );
        assertEquals(List.of(current), repository.findByUavId(UAV_ID));
    }

    @Test
    void returnsUnmodifiableHistorySnapshots() {
        Telemetry first = telemetry(FIRST_UPDATE);
        repository.save(first);
        List<Telemetry> snapshot = repository.findByUavId(UAV_ID);

        repository.save(telemetry(FIRST_UPDATE.plusSeconds(1)));

        assertEquals(List.of(first), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(first));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertThrows(NullPointerException.class, () -> repository.findByUavId(null));
        assertThrows(NullPointerException.class, () -> repository.findLatestByUavId(null));
    }

    private Telemetry telemetry(Instant lastUpdate) {
        return new Telemetry(
                UAV_ID,
                new Position(42.29, -8.74, 50),
                UavStatus.FLYING,
                (byte) 75,
                lastUpdate
        );
    }
}
