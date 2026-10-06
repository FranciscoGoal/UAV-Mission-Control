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

class LandCommandTest {

    private static final UUID ID = UUID.fromString(
            "00000000-0000-0000-0000-000000000001"
    );
    private static final Instant CREATED_AT = Instant.parse("2026-10-06T18:00:00Z");
    private static final Instant EXPIRES_AT = CREATED_AT.plusSeconds(10);

    @Test
    void preservesValuesAndReportsType() {
        LandCommand command = command();

        assertEquals(ID, command.id());
        assertEquals("uav-1", command.uavId());
        assertEquals(CREATED_AT, command.createdAt());
        assertEquals(EXPIRES_AT, command.expiresAt());
        assertEquals(CommandType.LAND, command.type());
    }

    @Test
    void factoryCreatesValidCommandsWithUniqueIds() {
        Instant before = Instant.now();
        LandCommand first = LandCommand.create("uav-1");
        LandCommand second = LandCommand.create("uav-1");
        Instant after = Instant.now();

        assertNotNull(first.id());
        assertFalse(first.id().equals(second.id()));
        assertFalse(first.createdAt().isBefore(before));
        assertFalse(first.createdAt().isAfter(after));
        assertEquals(first.createdAt().plusSeconds(10), first.expiresAt());
    }

    @Test
    void rejectsNullRequiredValues() {
        assertThrows(NullPointerException.class,
                () -> new LandCommand(null, "uav-1", CREATED_AT, EXPIRES_AT));
        assertThrows(IllegalArgumentException.class,
                () -> new LandCommand(ID, null, CREATED_AT, EXPIRES_AT));
        assertThrows(NullPointerException.class,
                () -> new LandCommand(ID, "uav-1", null, EXPIRES_AT));
        assertThrows(NullPointerException.class,
                () -> new LandCommand(ID, "uav-1", CREATED_AT, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void rejectsBlankUavId(String uavId) {
        assertThrows(IllegalArgumentException.class,
                () -> new LandCommand(ID, uavId, CREATED_AT, EXPIRES_AT));
    }

    @Test
    void rejectsInvalidExpiration() {
        assertThrows(IllegalArgumentException.class,
                () -> new LandCommand(ID, "uav-1", CREATED_AT, CREATED_AT));
        assertThrows(IllegalArgumentException.class,
                () -> new LandCommand(ID, "uav-1", CREATED_AT, CREATED_AT.minusSeconds(1)));
    }

    private LandCommand command() {
        return new LandCommand(ID, "uav-1", CREATED_AT, EXPIRES_AT);
    }
}
