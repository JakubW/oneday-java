package com.oneday.service;

import com.oneday.config.ErrorMessageProperties;
import com.oneday.config.ServiceMessageProperties;
import com.oneday.model.AltitudeOffsetRange;
import com.oneday.model.PostalTemperature;
import com.oneday.repository.AltitudeOffsetRangeRepository;
import com.oneday.repository.PostalTemperatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for temperature-related operations based on postal codes and altitude.
 */
@Service
public class TemperatureService {

    private static final Logger log = LoggerFactory.getLogger(TemperatureService.class);

    private final PostalTemperatureRepository repository;
    private final MapService mapService;
    private final AltitudeOffsetRangeRepository altitudeOffsetRangeRepository;
    private final ErrorMessageProperties errorMessages;
    private final ServiceMessageProperties serviceMessages;

    public TemperatureService(PostalTemperatureRepository repository, MapService mapService,
                              AltitudeOffsetRangeRepository altitudeOffsetRangeRepository,
                              ErrorMessageProperties errorMessages, ServiceMessageProperties serviceMessages) {
        this.repository = repository;
        this.mapService = mapService;
        this.altitudeOffsetRangeRepository = altitudeOffsetRangeRepository;
        this.errorMessages = errorMessages;
        this.serviceMessages = serviceMessages;
    }

    /**
     * Get the standard minimum temperature for a given postal code and address.
     * The temperature is adjusted based on altitude offset.
     *
     * @param postalCode the postal code (first 2 digits used)
     * @param address the address to calculate altitude from
     * @return adjusted minimum temperature in Celsius
     * @throws IllegalArgumentException if postal code is not found
     */
    public double getStandardMinTemperature(String postalCode, String address) throws IllegalArgumentException {
        String normalizedPostalCode = normalizePostalPrefix(postalCode);
        double baseTemperature = getBaseTemperatureOrThrow(normalizedPostalCode);

        int altitude = mapService.getAltitudeMeters(address);
        double temperatureOffset = getAltitudeOffsetForMeters(altitude);

        double adjustedTemperature = baseTemperature + temperatureOffset;

        log.debug("Temperature calculation for postal code {}: base={}, altitude={}, offset={}, result={}",
                normalizedPostalCode, baseTemperature, altitude, temperatureOffset, adjustedTemperature);

        return adjustedTemperature;
    }

    /**
     * Get base temperature for postal code from database.
     *
     * @param postalCode normalized postal code (2 digits)
     * @return base minimum temperature in Celsius
     * @throws IllegalArgumentException if postal code not found
     */
    private double getBaseTemperatureOrThrow(String postalCode) throws IllegalArgumentException {
        return repository.findById(postalCode)
                .map(PostalTemperature::getTemperature)
                .orElseThrow(() -> createMissingPostalCodeException(postalCode));
    }

    /**
     * Create an exception for missing postal code with proper logging.
     */
    private IllegalArgumentException createMissingPostalCodeException(String postalCode) {
        log.warn("Postal code prefix '{}' not found in database", postalCode);
        return new IllegalArgumentException(errorMessages.getPostalCodeNotFound());
    }

    /**
     * Normalize postal code to first 2 digits.
     */
    private String normalizePostalPrefix(String postalCode) {
        if (postalCode == null) return "";
        postalCode = postalCode.trim();
        if (postalCode.length() >= 2) {
            return postalCode.substring(0, 2);
        }
        return postalCode;
    }

    /**
     * Get altitude offset for the given altitude in meters.
     * Looks up the offset from the configured altitude offset ranges.
     *
     * @param altitude altitude in meters
     * @return temperature offset in Celsius
     * @throws IllegalArgumentException if altitude exceeds maximum configured range
     */
    private double getAltitudeOffsetForMeters(int altitude) throws IllegalArgumentException {
        List<AltitudeOffsetRange> altitudeRanges = altitudeOffsetRangeRepository.findAllByOrderByFromMetersAsc();

        if (altitudeRanges == null || altitudeRanges.isEmpty()) {
            log.debug(serviceMessages.getTemperature().getNoOffsets());
            return 0;
        }

        validateAltitudeWithinMaxRange(altitude, altitudeRanges);

        return findAltitudeOffset(altitude, altitudeRanges);
    }

    /**
     * Validate that altitude doesn't exceed the maximum configured range.
     *
     * @throws IllegalArgumentException if altitude exceeds maximum
     */
    private void validateAltitudeWithinMaxRange(int altitude, List<AltitudeOffsetRange> altitudeRanges) {
        int maxAltitude = altitudeRanges.stream()
                .mapToInt(AltitudeOffsetRange::getToMeters)
                .max()
                .orElse(Integer.MAX_VALUE);

        if (altitude > maxAltitude) {
            log.warn(serviceMessages.getTemperature().getAltitudeExceeds(), altitude, maxAltitude);
            throw new IllegalArgumentException(
                String.format(errorMessages.getAltitudeExceed(), maxAltitude)
            );
        }
    }

    /**
     * Find the temperature offset for a given altitude from the list of ranges.
     *
     * @return temperature offset or 0 if no matching range found
     */
    private double findAltitudeOffset(int altitude, List<AltitudeOffsetRange> altitudeRanges) {
        for (AltitudeOffsetRange altitudeRange : altitudeRanges) {
            if (altitudeIsInRange(altitude, altitudeRange)) {
                log.debug("Found offset {} for altitude {} in range [{}, {}]",
                        altitudeRange.getOffset(), altitude,
                        altitudeRange.getFromMeters(), altitudeRange.getToMeters());
                return altitudeRange.getOffset();
            }
        }

        log.debug("No matching altitude offset range for altitude {}, returning 0", altitude);
        return 0;
    }

    /**
     * Check if altitude falls within the given range.
     */
    private boolean altitudeIsInRange(int altitude, AltitudeOffsetRange altitudeRange) {
        return altitude >= altitudeRange.getFromMeters() &&
               altitude <= altitudeRange.getToMeters();
    }
}