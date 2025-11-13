package com.oneday.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oneday.config.ApiProperties;
import com.oneday.config.ErrorMessageProperties;
import com.oneday.config.ServiceMessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Primary
public class MapOsmService implements MapService {

    private static final int MAX_ELEVATION_METERS = 20000;
    private static final int MIN_ELEVATION_METERS = -500;
    private static final String NOMINATIM_QUERY_PARAMS = "?q=%s&format=json&limit=1&addressdetails=0";

    private final RestTemplate restTemplate;
    private final Logger log = LoggerFactory.getLogger(MapOsmService.class);
    private final ApiProperties apiProperties;
    private final ServiceMessageProperties serviceMessages;
    private final ErrorMessageProperties errorMessages;

    public MapOsmService(RestTemplate restTemplate, ApiProperties apiProperties,
                         ServiceMessageProperties serviceMessages, ErrorMessageProperties errorMessages) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.serviceMessages = serviceMessages;
        this.errorMessages = errorMessages;
    }

    @Override
    public int getAltitudeMeters(String address) throws IllegalArgumentException {
        if (isInvalidAddress(address)) {
            return 0;
        }

        try {
            NominatimResult nominatimResult = geocodeAddress(address);
            int elevation = getElevationFromCoordinates(nominatimResult.getLat(), nominatimResult.getLon());
            return validateAndReturnElevation(elevation);
        } catch (RestClientException e) {
            log.error("External maps API call failed", e);
            return 0;
        } catch (Exception e) {
            log.error("Unexpected error while getting altitude", e);
            return 0;
        }
    }

    /**
     * Check if the address is valid (not null or blank).
     */
    private boolean isInvalidAddress(String address) {
        return address == null || address.isBlank();
    }

    /**
     * Geocode address using Nominatim API to get latitude and longitude.
     *
     * @param address the address to geocode
     * @return NominatimResult containing lat/lon
     * @throws IllegalArgumentException if address cannot be geocoded
     */
    private NominatimResult geocodeAddress(String address) {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String nominatimUrl = buildNominatimUrl(encodedAddress);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, apiProperties.getUserAgent());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<NominatimResult[]> response = restTemplate.exchange(
            nominatimUrl, HttpMethod.GET, entity, NominatimResult[].class
        );
        NominatimResult[] results = response.getBody();

        return validateNominatimResults(results, address);
    }

    /**
     * Build the complete Nominatim API URL with query parameters.
     */
    private String buildNominatimUrl(String encodedAddress) {
        return apiProperties.getNominatimUrl() +
            String.format(NOMINATIM_QUERY_PARAMS, encodedAddress);
    }

    /**
     * Validate Nominatim results and return the first result.
     *
     * @throws IllegalArgumentException if no results found
     */
    private NominatimResult validateNominatimResults(NominatimResult[] results, String address) {
        if (results == null || results.length == 0) {
            log.warn(serviceMessages.getNominatim().getNoResults(), address);
            throw new IllegalArgumentException("Address not found: " + address);
        }
        return results[0];
    }

    /**
     * Get elevation in meters from Open-Elevation API.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @return elevation in meters
     */
    private int getElevationFromCoordinates(String latitude, String longitude) {
        String elevationUrl = buildElevationUrl(latitude, longitude);
        ElevationResponse elevationResponse = restTemplate.getForObject(
            elevationUrl, ElevationResponse.class
        );

        return extractElevation(elevationResponse, latitude, longitude);
    }

    /**
     * Build the complete Open-Elevation API URL.
     */
    private String buildElevationUrl(String latitude, String longitude) {
        return apiProperties.getElevationUrl() + "?locations=" + latitude + "," + longitude;
    }

    /**
     * Extract elevation value from API response.
     *
     * @throws IllegalArgumentException if no elevation data found
     */
    private int extractElevation(ElevationResponse elevationResponse,
                                 String latitude, String longitude) {
        if (elevationResponse == null || elevationResponse.getResults() == null ||
            elevationResponse.getResults().length == 0) {
            log.warn(serviceMessages.getElevation().getNoResult(), latitude, longitude);
            throw new IllegalArgumentException("Elevation data not found for coordinates: " + latitude + "," + longitude);
        }
        return (int) Math.round(elevationResponse.getResults()[0].getElevation());
    }

    /**
     * Validate elevation is within acceptable bounds.
     *
     * @param elevation elevation in meters
     * @return the elevation if valid
     * @throws IllegalArgumentException if elevation is invalid
     */
    private int validateAndReturnElevation(int elevation) {
        if (!isValidElevation(elevation)) {
            log.error("Invalid elevation value: {}. Must be between {} and {} meters",
                elevation, MIN_ELEVATION_METERS, MAX_ELEVATION_METERS);
            throw new IllegalArgumentException(errorMessages.getInvalidElevation());
        }
        return elevation;
    }

    /**
     * Check if elevation is within acceptable bounds.
     * Realworld elevations range from ~-430m (Dead Sea) to ~8,849m (Mt. Everest)
     */
    private boolean isValidElevation(int elevation) {
        return elevation >= MIN_ELEVATION_METERS && elevation <= MAX_ELEVATION_METERS;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NominatimResult {
        private String lat;
        private String lon;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }
    }

    /**
     * Response from Open-Elevation API.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ElevationResponse {
        private ElevationResult[] results;

        public ElevationResult[] getResults() {
            return results;
        }

        public void setResults(ElevationResult[] results) {
            this.results = results;
        }
    }

    /**
     * Single elevation result from Open-Elevation API.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ElevationResult {
        private double elevation;

        public double getElevation() {
            return elevation;
        }

        public void setElevation(double elevation) {
            this.elevation = elevation;
        }
    }
}
