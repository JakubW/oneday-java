package com.oneday.service;

import com.oneday.model.AltitudeOffsetRange;
import com.oneday.model.PostalTemperature;
import com.oneday.repository.AltitudeOffsetRangeRepository;
import com.oneday.repository.PostalTemperatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for temperature-related operations based on postal codes and altitude.
 */
@Service
public class TemperatureService {

    private static final Logger log = LoggerFactory.getLogger(TemperatureService.class);

    private final PostalTemperatureRepository repository;
    private final MapService mapService;
    private final AltitudeOffsetRangeRepository altitudeOffsetRangeRepository;

    public TemperatureService(PostalTemperatureRepository repository, MapService mapService, AltitudeOffsetRangeRepository altitudeOffsetRangeRepository) {
        this.repository = repository;
        this.mapService = mapService;
        this.altitudeOffsetRangeRepository = altitudeOffsetRangeRepository;
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
        String key = normalizePostalPrefix(postalCode);
        Optional<PostalTemperature> pt = repository.findById(key);

        double base = pt.map(PostalTemperature::getTemperature)
                .orElseThrow(() -> {
                    log.warn("Postal code prefix '{}' not found in database", key);
                    return new IllegalArgumentException("Postal Code prefix not found in temperature data or vice versa.");
                });

        int altitude = mapService.getAltitudeMeters(address);
        double offset = altitudeOffsetForMeters(altitude);

        log.debug("Temperature calculation for postal code {}: base={}, altitude={}, offset={}, result={}",
                key, base, altitude, offset, base + offset);

        return base + offset;
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
    private double altitudeOffsetForMeters(int altitude) throws IllegalArgumentException {
        List<AltitudeOffsetRange> ranges = altitudeOffsetRangeRepository.findAllByOrderByFromMetersAsc();

        if (ranges == null || ranges.isEmpty()) {
            log.debug("No altitude offset ranges configured, returning 0");
            return 0;
        }

        int maxTo = ranges.stream().mapToInt(AltitudeOffsetRange::getToMeters).max().orElse(Integer.MAX_VALUE);
        if (altitude > maxTo) {
            log.warn("Altitude {} exceeds maximum configured altitude {}", altitude, maxTo);
            throw new IllegalArgumentException("Altitude exceed " + maxTo + " meters, no temperature offset data available.");
        }

        for (AltitudeOffsetRange r : ranges) {
            if (altitude >= r.getFromMeters() && altitude <= r.getToMeters()) {
                log.debug("Found offset {} for altitude {} in range [{}, {}]",
                        r.getOffset(), altitude, r.getFromMeters(), r.getToMeters());
                return r.getOffset();
            }
        }

        log.debug("No matching altitude offset range for altitude {}, returning 0", altitude);
        return 0;
    }
}