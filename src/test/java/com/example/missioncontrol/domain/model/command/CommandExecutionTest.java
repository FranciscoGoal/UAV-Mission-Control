package com.example.missioncontrol.domain.model.command;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandExecutionTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-06T18:00:00Z");
    private static final Instant UPDATED_AT = CREATED_AT.plusSeconds(2);

    @Test
    void preservesRequiredValues() {
        TakeOffCommand command = command();
        CommandExecution execution = new CommandExecution(
                command,
                CommandStatus.SENT,
                "operator-1",
                List.of("priority=high"),
                UPDATED_AT
        );

        assertSame(command, execution.command());
        assertEquals(CommandStatus.SENT, execution.status());
        assertEquals("operator-1", execution.operatorSesionId());
        assertEquals(List.of("priority=high"), execution.parameters());
        assertEquals(UPDATED_AT, execution.updatedAt());
    }

    @Test
    void rejectsNullRequiredValues() {
        TakeOffCommand command = command();

        assertThrows(NullPointerException.class,
                () -> new CommandExecution(null, CommandStatus.SENT, "operator-1", List.of(), UPDATED_AT));
        assertThrows(NullPointerException.class,
                () -> new CommandExecution(command, null, "operator-1", List.of(), UPDATED_AT));
        assertThrows(NullPointerException.class,
                () -> new CommandExecution(command, CommandStatus.SENT, null, List.of(), UPDATED_AT));
        assertThrows(NullPointerException.class,
                () -> new CommandExecution(command, CommandStatus.SENT, "operator-1", null, UPDATED_AT));
        assertThrows(NullPointerException.class,
                () -> new CommandExecution(command, CommandStatus.SENT, "operator-1", List.of(), null));
    }

    @Test
    void defensivelyCopiesParameters() {
        List<String> parameters = new ArrayList<>(List.of("priority=high"));
        CommandExecution execution = new CommandExecution(
                command(), CommandStatus.SENT, "operator-1", parameters, UPDATED_AT
        );

        parameters.add("retry=true");

        assertEquals(List.of("priority=high"), execution.parameters());
        assertThrows(UnsupportedOperationException.class,
                () -> execution.parameters().add("retry=true"));
    }

    @Test
    void rejectsNullParameterElements() {
        List<String> parameters = new ArrayList<>();
        parameters.add(null);

        assertThrows(NullPointerException.class,
                () -> new CommandExecution(
                        command(), CommandStatus.SENT, "operator-1", parameters, UPDATED_AT
                ));
    }

    private TakeOffCommand command() {
        return new TakeOffCommand(
                UUID.randomUUID(), "uav-1", 25, CREATED_AT, CREATED_AT.plusSeconds(10)
        );
    }
}
