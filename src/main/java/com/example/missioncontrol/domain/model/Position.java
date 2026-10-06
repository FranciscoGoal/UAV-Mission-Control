package com.example.missioncontrol.domain.model;

public record Position(
        double latitude,
        double longitude,
        double altitude
) {
    public Position {
        if (!Double.isFinite(latitude)
                || latitude < -90
                || latitude > 90) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (!Double.isFinite(longitude)
                || longitude < -180
                || longitude > 180) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180"
            );
        }

        if (!Double.isFinite(altitude) || altitude < 0) {
            throw new IllegalArgumentException(
                    "Altitude cannot be negative"
            );
        }
    }
}
