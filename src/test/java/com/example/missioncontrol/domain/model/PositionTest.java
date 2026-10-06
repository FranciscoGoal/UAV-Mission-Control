package com.example.missioncontrol.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {

    @Test
    void preservesValidCoordinates() {
        Position position = new Position(42.28, -8.73, 20);

        assertEquals(42.28, position.latitude());
        assertEquals(-8.73, position.longitude());
        assertEquals(20, position.altitude());
    }

    @Test
    void acceptsBoundaryCoordinates() {
        assertDoesNotThrow(() -> new Position(-90, -180, 0));
        assertDoesNotThrow(() -> new Position(90, 180, Double.MAX_VALUE));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-90.0001, 90.0001, Double.NaN,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    void rejectsInvalidLatitude(double latitude) {
        assertThrows(IllegalArgumentException.class,
                () -> new Position(latitude, 0, 0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-180.0001, 180.0001, Double.NaN,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    void rejectsInvalidLongitude(double longitude) {
        assertThrows(IllegalArgumentException.class,
                () -> new Position(0, longitude, 0));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.0001, Double.NaN,
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    void rejectsInvalidAltitude(double altitude) {
        assertThrows(IllegalArgumentException.class,
                () -> new Position(0, 0, altitude));
    }
}
