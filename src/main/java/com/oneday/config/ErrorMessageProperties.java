package com.oneday.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for error messages.
 * Prefix: app.error
 */
@Component
@ConfigurationProperties(prefix = "app.error")
public class ErrorMessageProperties {

    private String validation;
    private String internalServer;
    private String unexpected;
    private String postalCodeNotFound;
    private String altitudeExceed;
    private String invalidElevation;
    private String addressPostalCodeRequired;

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getInternalServer() {
        return internalServer;
    }

    public void setInternalServer(String internalServer) {
        this.internalServer = internalServer;
    }

    public String getUnexpected() {
        return unexpected;
    }

    public void setUnexpected(String unexpected) {
        this.unexpected = unexpected;
    }

    public String getPostalCodeNotFound() {
        return postalCodeNotFound;
    }

    public void setPostalCodeNotFound(String postalCodeNotFound) {
        this.postalCodeNotFound = postalCodeNotFound;
    }

    public String getAltitudeExceed() {
        return altitudeExceed;
    }

    public void setAltitudeExceed(String altitudeExceed) {
        this.altitudeExceed = altitudeExceed;
    }

    public String getInvalidElevation() {
        return invalidElevation;
    }

    public void setInvalidElevation(String invalidElevation) {
        this.invalidElevation = invalidElevation;
    }

    public String getAddressPostalCodeRequired() {
        return addressPostalCodeRequired;
    }

    public void setAddressPostalCodeRequired(String addressPostalCodeRequired) {
        this.addressPostalCodeRequired = addressPostalCodeRequired;
    }
}

