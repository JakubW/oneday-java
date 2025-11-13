package com.oneday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for dataset file paths.
 * Prefix: app.datasets
 */
@Component
@ConfigurationProperties(prefix = "app.datasets")
public class DatasetProperties {

    private String temperatures;
    private String offsets;

    public String getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(String temperatures) {
        this.temperatures = temperatures;
    }

    public String getOffsets() {
        return offsets;
    }

    public void setOffsets(String offsets) {
        this.offsets = offsets;
    }
}

