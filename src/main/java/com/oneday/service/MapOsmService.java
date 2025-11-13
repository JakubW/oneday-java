package com.oneday.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private final RestTemplate restTemplate;
    private final Logger log = LoggerFactory.getLogger(MapOsmService.class);

    @Value("${app.nominatim.url}")
    private String nominatimUrl;

    @Value("${app.elevation.url}")
    private String elevationUrl;

    @Value("${app.user-agent}")
    private String userAgent;

    @Value("${nominatim.no-results}")
    private String nominatimNoResults;

    @Value("${elevation.no-result}")
    private String elevationNoResult;

    @Value("${error.invalid-elevation}")
    private String invalidElevationError;

    public MapOsmService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public int getAltitudeMeters(String address) throws IllegalArgumentException {
        if (address == null || address.isBlank()) return 0;
        try {
            // 1) Use Nominatim to geocode address -> lat/lon
            String q = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String nominatimUrlWithParams = nominatimUrl + "?q=" + q + "&format=json&limit=1&addressdetails=0";

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, userAgent);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<NominatimResult[]> resp = restTemplate.exchange(nominatimUrlWithParams, HttpMethod.GET, entity, NominatimResult[].class);
            NominatimResult[] results = resp.getBody();

            if (results == null || results.length == 0) {
                log.warn(nominatimNoResults, address);
                return 0;
            }
            double lat = Double.parseDouble(results[0].lat);
            double lon = Double.parseDouble(results[0].lon);

            // 2) Use Open-Elevation
            String elevationUrlWithParams = elevationUrl + "?locations=" + lat + "," + lon;
            ElevationResponse er = restTemplate.getForObject(elevationUrlWithParams, ElevationResponse.class);
            if (er == null || er.results == null || er.results.length == 0) {
                log.warn(elevationNoResult, lat, lon);
                return 0;
            }
            int elevation = (int) Math.round(er.results[0].elevation);
            if (elevation > 20000 || elevation < -500) {
                // clearly bogus
                throw new IllegalArgumentException(invalidElevationError);
            }
            return elevation;
        } catch (RestClientException e) {
            log.error("External maps call failed", e);
            return 0;
        } catch (IllegalArgumentException e) {
            // rethrow known validation exceptions
            throw e;
        } catch (Exception e) {
            log.error("Failed to get altitude", e);
            return 0;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NominatimResult {
        public String lat;
        public String lon;
    }

    /**
     * Response from Open-Elevation API.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ElevationResponse {
        public ElevationResult[] results;
    }

    /**
     * Single elevation result from Open-Elevation API.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ElevationResult {
        public double elevation;
    }
}
