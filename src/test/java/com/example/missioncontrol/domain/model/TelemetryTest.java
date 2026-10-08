package com.example.missioncontrol.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TelemetryTest {

    private static final UUID UAV_ID = UUID.randomUUID();
    private static final Position POSITION = new Position(42.29, -8.74, 50);
    private static final Instant LAST_UPDATE = Instant.parse("2026-10-09T00:30:00Z");

    @Test
    void createsValidTelemetrySnapshot() {
        Telemetry telemetry = telemetry((byte) 75);

        assertEquals(UAV_ID, telemetry.uavId());
        assertEquals(POSITION, telemetry.position());
        assertEquals(UavStatus.FLYING, telemetry.status());
        assertEquals((byte) 75, telemetry.battery());
        assertEquals(LAST_UPDATE, telemetry.lastUpdate());
    }

    @ParameterizedTest
    @ValueSource(bytes = {-1, 101, 127})
    void rejectsBatteryOutsidePercentageRange(byte battery) {
        assertThrows(IllegalArgumentException.class, () -> telemetry(battery));
    }

    @Test
    void rejectsNullValues() {
        assertThrows(NullPointerException.class, () -> new Telemetry(
                null, POSITION, UavStatus.FLYING, (byte) 75, LAST_UPDATE
        ));
        assertThrows(NullPointerException.class, () -> new Telemetry(
                UAV_ID, null, UavStatus.FLYING, (byte) 75, LAST_UPDATE
        ));
        assertThrows(NullPointerException.class, () -> new Telemetry(
                UAV_ID, POSITION, null, (byte) 75, LAST_UPDATE
        ));
        assertThrows(NullPointerException.class, () -> new Telemetry(
                UAV_ID, POSITION, UavStatus.FLYING, null, LAST_UPDATE
        ));
        assertThrows(NullPointerException.class, () -> new Telemetry(
                UAV_ID, POSITION, UavStatus.FLYING, (byte) 75, null
        ));
    }

    private Telemetry telemetry(byte battery) {
        return new Telemetry(
                UAV_ID, POSITION, UavStatus.FLYING, battery, LAST_UPDATE
        );
    }
}
