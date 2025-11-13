package com.oneday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for service-specific messages.
 * Prefix: app.service
 */
@Component
@ConfigurationProperties(prefix = "app.service")
public class ServiceMessageProperties {

    private Nominatim nominatim = new Nominatim();
    private Elevation elevation = new Elevation();
    private Temperature temperature = new Temperature();

    public Nominatim getNominatim() {
        return nominatim;
    }

    public void setNominatim(Nominatim nominatim) {
        this.nominatim = nominatim;
    }

    public Elevation getElevation() {
        return elevation;
    }

    public void setElevation(Elevation elevation) {
        this.elevation = elevation;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    /**
     * Nominatim service messages.
     */
    public static class Nominatim {
        private String noResults;

        public String getNoResults() {
            return noResults;
        }

        public void setNoResults(String noResults) {
            this.noResults = noResults;
        }
    }

    /**
     * Elevation service messages.
     */
    public static class Elevation {
        private String noResult;

        public String getNoResult() {
            return noResult;
        }

        public void setNoResult(String noResult) {
            this.noResult = noResult;
        }
    }

    /**
     * Temperature service messages.
     */
    public static class Temperature {
        private String noOffsets;
        private String altitudeExceeds;

        public String getNoOffsets() {
            return noOffsets;
        }

        public void setNoOffsets(String noOffsets) {
            this.noOffsets = noOffsets;
        }

        public String getAltitudeExceeds() {
            return altitudeExceeds;
        }

        public void setAltitudeExceeds(String altitudeExceeds) {
            this.altitudeExceeds = altitudeExceeds;
        }
    }
}

