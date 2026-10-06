package com.example.missioncontrol.application.service;

import com.example.missioncontrol.domain.model.Position;
import com.example.missioncontrol.domain.model.Uav;
import com.example.missioncontrol.domain.model.UavStatus;
import com.example.missioncontrol.domain.model.repository.UavRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UavServiceTest {

    private static final UUID ID = UUID.fromString(
            "00000000-0000-0000-0000-000000000001"
    );
    private static final Position INITIAL_POSITION = new Position(42.28, -8.73, 0);

    private UavRepository repository;
    private UavService service;

    @BeforeEach
    void setUp() {
        repository = mock(UavRepository.class);
        service = new UavService(repository);
    }

    @Test
    void registersAndPersistsNewUav() {
        when(repository.findById(ID)).thenReturn(Optional.empty());
        when(repository.save(any(Uav.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Uav result = service.registerUav(ID, INITIAL_POSITION, UavStatus.FLYING);

        assertEquals(ID, result.getId());
        assertSame(INITIAL_POSITION, result.getPosition());
        assertEquals(UavStatus.FLYING, result.getStatus());
        verify(repository).save(result);
    }

    @Test
    void rejectsDuplicateIdWithoutSaving() {
        Uav existing = uav();
        when(repository.findById(ID)).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.registerUav(ID, INITIAL_POSITION, UavStatus.GROUND)
        );

        assertEquals("A UAV with this id already exists: " + ID, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsInvalidRegistrationDataWithoutSaving() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class,
                () -> service.registerUav(ID, null, UavStatus.GROUND));
        assertThrows(NullPointerException.class,
                () -> service.registerUav(ID, INITIAL_POSITION, null));
        verify(repository, never()).save(any());
    }

    @Test
    void getsRegisteredUav() {
        Uav existing = uav();
        when(repository.findById(ID)).thenReturn(Optional.of(existing));

        assertSame(existing, service.getUav(ID));
    }

    @Test
    void getUavRejectsUnknownId() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getUav(ID)
        );

        assertEquals("UAV not registered: " + ID, exception.getMessage());
    }

    @Test
    void updatesAndPersistsPosition() {
        Uav existing = uav();
        Position newPosition = new Position(42.29, -8.74, 20);
        when(repository.findById(ID)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Uav result = service.updatePosition(ID, newPosition);

        assertSame(existing, result);
        assertSame(newPosition, result.getPosition());
        verify(repository).save(existing);
    }

    @Test
    void updatePositionRejectsUnknownUavWithoutSaving() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.updatePosition(ID, INITIAL_POSITION));
        verify(repository, never()).save(any());
    }

    @Test
    void updatePositionRejectsNullWithoutSaving() {
        Uav existing = uav();
        when(repository.findById(ID)).thenReturn(Optional.of(existing));

        assertThrows(NullPointerException.class,
                () -> service.updatePosition(ID, null));
        assertSame(INITIAL_POSITION, existing.getPosition());
        verify(repository, never()).save(any());
    }

    @Test
    void updatesAndPersistsStatus() {
        Uav existing = uav();
        when(repository.findById(ID)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Uav result = service.updateStatus(ID, UavStatus.TAKING_OFF);

        assertSame(existing, result);
        assertEquals(UavStatus.TAKING_OFF, result.getStatus());
        verify(repository).save(existing);
    }

    @Test
    void updateStatusRejectsUnknownUavWithoutSaving() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(ID, UavStatus.TAKING_OFF));
        verify(repository, never()).save(any());
    }

    @Test
    void updateStatusRejectsNullWithoutSaving() {
        Uav existing = uav();
        when(repository.findById(ID)).thenReturn(Optional.of(existing));

        assertThrows(NullPointerException.class,
                () -> service.updateStatus(ID, null));
        assertEquals(UavStatus.GROUND, existing.getStatus());
        verify(repository, never()).save(any());
    }

    private Uav uav() {
        return new Uav(ID, INITIAL_POSITION, UavStatus.GROUND);
    }
}
