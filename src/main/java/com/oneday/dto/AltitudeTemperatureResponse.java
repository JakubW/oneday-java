package com.oneday.dto;

/**
 * Response DTO for altitude and temperature data.
 */
public class AltitudeTemperatureResponse {

    private int altitude;
    private double standardMinTemperature;

    public AltitudeTemperatureResponse() {}

    public AltitudeTemperatureResponse(int altitude, double standardMinTemperature) {
        this.altitude = altitude;
        this.standardMinTemperature = standardMinTemperature;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public double getStandardMinTemperature() {
        return standardMinTemperature;
    }

    public void setStandardMinTemperature(double standardMinTemperature) {
        this.standardMinTemperature = standardMinTemperature;
    }
}

