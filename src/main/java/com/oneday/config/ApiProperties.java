package com.oneday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for external API integrations.
 * Prefix: app.api
 */
@Component
@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {

    private String nominatimUrl;
    private String elevationUrl;
    private String userAgent;

    public String getNominatimUrl() {
        return nominatimUrl;
    }

    public void setNominatimUrl(String nominatimUrl) {
        this.nominatimUrl = nominatimUrl;
    }

    public String getElevationUrl() {
        return elevationUrl;
    }

    public void setElevationUrl(String elevationUrl) {
        this.elevationUrl = elevationUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}

