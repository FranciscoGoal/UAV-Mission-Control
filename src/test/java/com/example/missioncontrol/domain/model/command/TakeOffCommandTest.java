package com.example.missioncontrol.domain.model.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TakeOffCommandTest {

    private static final UUID ID = UUID.fromString(
            "00000000-0000-0000-0000-000000000001"
    );
    private static final Instant CREATED_AT = Instant.parse("2026-10-06T18:00:00Z");
    private static final Instant EXPIRES_AT = CREATED_AT.plusSeconds(10);

    @Test
    void preservesValuesAndReportsType() {
        TakeOffCommand command = command(25);

        assertEquals(ID, command.id());
        assertEquals("uav-1", command.uavId());
        assertEquals(25, command.targetAltitudeMeters());
        assertEquals(CREATED_AT, command.createdAt());
        assertEquals(EXPIRES_AT, command.expiresAt());
        assertEquals(CommandType.TAKE_OFF, command.type());
    }

    @Test
    void factoryCreatesValidCommandsWithUniqueIds() {
        Instant before = Instant.now();
        TakeOffCommand first = TakeOffCommand.create("uav-1", 25);
        TakeOffCommand second = TakeOffCommand.create("uav-1", 25);
        Instant after = Instant.now();

        assertNotNull(first.id());
        assertFalse(first.id().equals(second.id()));
        assertEquals("uav-1", first.uavId());
        assertEquals(25, first.targetAltitudeMeters());
        assertFalse(first.createdAt().isBefore(before));
        assertFalse(first.createdAt().isAfter(after));
        assertEquals(first.createdAt().plusSeconds(10), first.expiresAt());
    }

    @Test
    void rejectsNullRequiredValues() {
        assertThrows(NullPointerException.class,
                () -> new TakeOffCommand(null, "uav-1", 25, CREATED_AT, EXPIRES_AT));
        assertThrows(IllegalArgumentException.class,
                () -> new TakeOffCommand(ID, null, 25, CREATED_AT, EXPIRES_AT));
        assertThrows(NullPointerException.class,
                () -> new TakeOffCommand(ID, "uav-1", 25, null, EXPIRES_AT));
        assertThrows(NullPointerException.class,
                () -> new TakeOffCommand(ID, "uav-1", 25, CREATED_AT, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void rejectsBlankUavId(String uavId) {
        assertThrows(IllegalArgumentException.class,
                () -> new TakeOffCommand(ID, uavId, 25, CREATED_AT, EXPIRES_AT));
    }

    @Test
    void rejectsInvalidExpiration() {
        assertThrows(IllegalArgumentException.class,
                () -> new TakeOffCommand(ID, "uav-1", 25, CREATED_AT, CREATED_AT));
        assertThrows(IllegalArgumentException.class,
                () -> new TakeOffCommand(ID, "uav-1", 25, CREATED_AT, CREATED_AT.minusSeconds(1)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -1, Double.NaN,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    void rejectsInvalidTargetAltitude(double altitude) {
        assertThrows(IllegalArgumentException.class,
                () -> new TakeOffCommand(ID, "uav-1", altitude, CREATED_AT, EXPIRES_AT));
    }

    private TakeOffCommand command(double altitude) {
        return new TakeOffCommand(ID, "uav-1", altitude, CREATED_AT, EXPIRES_AT);
    }
}
