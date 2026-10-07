package com.example.missioncontrol.application.service;

import com.example.missioncontrol.domain.model.command.CommandExecution;
import com.example.missioncontrol.domain.model.command.CommandStatus;
import com.example.missioncontrol.domain.model.command.UavCommand;
import com.example.missioncontrol.domain.model.repository.CommandExecutionRepository;
import com.example.missioncontrol.domain.model.repository.CommandRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Service 
public class CommandService {

    private final CommandRepository commandRepository;
    private final CommandExecutionRepository commandExecutionRepository;

    public CommandService(CommandRepository commandRepository,
                          CommandExecutionRepository commandExecutionRepository) {
        this.commandRepository = commandRepository;
        this.commandExecutionRepository = commandExecutionRepository;
    }

    public synchronized CommandExecution create(
        UUID commandId,
        String operatorSessionId
    ) {
        
        UavCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("Comando no encontrado con ID: " + commandId));

        
        CommandExecution execution = new CommandExecution(
                command,
                CommandStatus.PENDING,
                operatorSessionId,
                Collections.emptyList(),
                Instant.now()
        );

        
        return commandExecutionRepository.save(execution);
    }
}
