package com.example.missioncontrol.domain.model.command;

import com.example.missioncontrol.domain.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoToCommandTest {

    private static final UUID ID = UUID.fromString(
            "00000000-0000-0000-0000-000000000001"
    );
    private static final Position TARGET_POSITION = new Position(42.29, -8.74, 50);
    private static final Instant CREATED_AT = Instant.parse("2026-10-06T18:00:00Z");
    private static final Instant EXPIRES_AT = CREATED_AT.plusSeconds(10);

    @Test
    void preservesValuesAndReportsType() {
        GoToCommand command = command();

        assertEquals(ID, command.id());
        assertEquals("uav-1", command.uavId());
        assertSame(TARGET_POSITION, command.targetPosition());
        assertEquals(CREATED_AT, command.createdAt());
        assertEquals(EXPIRES_AT, command.expiresAt());
        assertEquals(CommandType.GO_TO, command.type());
    }

    @Test
    void factoryCreatesValidCommandsWithUniqueIds() {
        Instant before = Instant.now();
        GoToCommand first = GoToCommand.create("uav-1", TARGET_POSITION);
        GoToCommand second = GoToCommand.create("uav-1", TARGET_POSITION);
        Instant after = Instant.now();

        assertNotNull(first.id());
        assertFalse(first.id().equals(second.id()));
        assertSame(TARGET_POSITION, first.targetPosition());
        assertFalse(first.createdAt().isBefore(before));
        assertFalse(first.createdAt().isAfter(after));
        assertEquals(first.createdAt().plusSeconds(10), first.expiresAt());
    }

    @Test
    void rejectsNullRequiredValues() {
        assertThrows(NullPointerException.class,
                () -> new GoToCommand(null, "uav-1", TARGET_POSITION, CREATED_AT, EXPIRES_AT));
        assertThrows(IllegalArgumentException.class,
                () -> new GoToCommand(ID, null, TARGET_POSITION, CREATED_AT, EXPIRES_AT));
        assertThrows(NullPointerException.class,
                () -> new GoToCommand(ID, "uav-1", null, CREATED_AT, EXPIRES_AT));
        assertThrows(NullPointerException.class,
                () -> new GoToCommand(ID, "uav-1", TARGET_POSITION, null, EXPIRES_AT));
        assertThrows(NullPointerException.class,
                () -> new GoToCommand(ID, "uav-1", TARGET_POSITION, CREATED_AT, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void rejectsBlankUavId(String uavId) {
        assertThrows(IllegalArgumentException.class,
                () -> new GoToCommand(ID, uavId, TARGET_POSITION, CREATED_AT, EXPIRES_AT));
    }

    @Test
    void rejectsInvalidExpiration() {
        assertThrows(IllegalArgumentException.class,
                () -> new GoToCommand(ID, "uav-1", TARGET_POSITION, CREATED_AT, CREATED_AT));
        assertThrows(IllegalArgumentException.class,
                () -> new GoToCommand(
                        ID, "uav-1", TARGET_POSITION, CREATED_AT, CREATED_AT.minusSeconds(1)
                ));
    }

    private GoToCommand command() {
        return new GoToCommand(
                ID, "uav-1", TARGET_POSITION, CREATED_AT, EXPIRES_AT
        );
    }
}
