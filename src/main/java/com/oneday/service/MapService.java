package com.oneday.service;

/**
 * Service interface for map and altitude-related operations.
 */
public interface MapService {
    /**
     * Get altitude in meters for a given address.
     *
     * @param address the address to lookup
     * @return altitude in meters, or 0 if unable to determine
     * @throws IllegalArgumentException if the address or altitude data is invalid
     */
    int getAltitudeMeters(String address) throws IllegalArgumentException;
}

