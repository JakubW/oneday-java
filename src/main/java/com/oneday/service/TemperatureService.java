package com.oneday.service;

import com.oneday.model.AltitudeOffsetRange;
import com.oneday.model.PostalTemperature;
import com.oneday.repository.AltitudeOffsetRangeRepository;
import com.oneday.repository.PostalTemperatureRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemperatureService {

    private final PostalTemperatureRepository repository;
    private final MapService mapService;
    private final AltitudeOffsetRangeRepository altitudeOffsetRangeRepository;

    public TemperatureService(PostalTemperatureRepository repository, MapService mapService, AltitudeOffsetRangeRepository altitudeOffsetRangeRepository) {
        this.repository = repository;
        this.mapService = mapService;
        this.altitudeOffsetRangeRepository = altitudeOffsetRangeRepository;
    }

    public double getStandardMinTemperature(String postalCode, String address) throws IllegalArgumentException {
        String key = normalizePostalPrefix(postalCode);
        Optional<PostalTemperature> pt = repository.findById(key);
//        double base = pt.map(PostalTemperature::getTemperature).orElse(0.0); //TODO wrong!
        double base = pt.map(PostalTemperature::getTemperature)
                .orElseThrow(() -> new IllegalArgumentException("Postal Code prefix not found in temperature data or vice versa."));
        int altitude = mapService.getAltitudeMeters(address);
        double offset = altitudeOffsetForMeters(altitude);
        return base + offset;
    }

    private String normalizePostalPrefix(String postalCode) {
        if (postalCode == null) return "";
        postalCode = postalCode.trim();
        if (postalCode.length() >= 2) {
            return postalCode.substring(0, 2);
        }
        return postalCode;
    }

    private double altitudeOffsetForMeters(int altitude) throws IllegalArgumentException {
        // Load offset ranges from DB (sorted by fromMeters)
        List<AltitudeOffsetRange> ranges = altitudeOffsetRangeRepository.findAllByOrderByFromMetersAsc();
        if (ranges == null || ranges.isEmpty()) {
            return 0; //TODO default?
        }

        // If altitude is greater than highest toMeters, throw
        int maxTo = ranges.stream().mapToInt(AltitudeOffsetRange::getToMeters).max().orElse(Integer.MAX_VALUE);
        if (altitude > maxTo) {
            throw new IllegalArgumentException("Altitude exceed " + maxTo + " meters, no temperature offset data available.");
        }

        for (AltitudeOffsetRange r : ranges) {
            if (altitude >= r.getFromMeters() && altitude <= r.getToMeters()) {
                return r.getOffset();
            }
        }
        // default fallback
        return 0; //TODO or throw?
    }
}