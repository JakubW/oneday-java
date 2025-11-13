package com.oneday.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity representing standard minimum temperature for a French postal code.
 * The postalCode field stores the first 2 digits of the French postal code (department).
 */
@Entity
public class PostalTemperature {

    @Id
    private String postalCode; // first two digits of French postal code (department)
    private double temperature;

    public PostalTemperature() {
    }

    public PostalTemperature(String postalCode, double temperature) {
        this.postalCode = postalCode;
        this.temperature = temperature;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}

