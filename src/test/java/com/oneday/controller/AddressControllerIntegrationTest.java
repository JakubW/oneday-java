package com.oneday.controller;

import com.oneday.model.PostalTemperature;
import com.oneday.repository.PostalTemperatureRepository;
import com.oneday.service.MapOsmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AddressControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private PostalTemperatureRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        // Load some test data
        repository.save(new PostalTemperature("75", -5.0)); // Paris
        repository.save(new PostalTemperature("38", -10.0)); // Grenoble
        repository.save(new PostalTemperature("13", -5.0)); // Marseille
    }

    @Test
    void testGetAltitudeAndTemperature_HappyPath_Success() throws Exception {
        // Mock Nominatim response
        MapOsmService.NominatimResult[] nominatimResults = new MapOsmService.NominatimResult[1];
        nominatimResults[0] = createNominatimResult("48.8566", "2.3522");

        when(restTemplate.exchange(
                contains("nominatim.openstreetmap.org"),
                eq(HttpMethod.GET),
                any(),
                eq(MapOsmService.NominatimResult[].class)
        )).thenReturn(ResponseEntity.ok(nominatimResults));

        // Mock Open-Elevation response
        when(restTemplate.getForObject(
                contains("api.open-elevation.com"),
                eq(MapOsmService.ElevationResponse.class)
        )).thenReturn(createElevationResponse(100.0));

        String requestBody = "{\"address\":\"10 Avenue des Champs-Élysées, Paris\",\"postalCode\":\"75\"}";

        mockMvc.perform(post("/api/v1/altitude-temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.altitude").value(100))
                .andExpect(jsonPath("$.standardMinTemperature").value(-5.0));
    }

    @Test
    void testGetAltitudeAndTemperature_AltitudeAbove1200_BadRequest() throws Exception {
        // Mock Nominatim response
        MapOsmService.NominatimResult[] nominatimResults = new MapOsmService.NominatimResult[1];
        nominatimResults[0] = createNominatimResult("45.0", "5.0");

        when(restTemplate.exchange(
                contains("nominatim.openstreetmap.org"),
                eq(HttpMethod.GET),
                any(),
                eq(MapOsmService.NominatimResult[].class)
        )).thenReturn(ResponseEntity.ok(nominatimResults));

        // Mock Open-Elevation response with altitude > 1200
        when(restTemplate.getForObject(
                contains("api.open-elevation.com"),
                eq(MapOsmService.ElevationResponse.class)
        )).thenReturn(createElevationResponse(1300.0));

        String requestBody = "{\"address\":\"High altitude location\",\"postalCode\":\"38\"}";

        mockMvc.perform(post("/api/v1/altitude-temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value(containsString("1200")));
    }

    @Test
    void testGetAltitudeAndTemperature_NeitherAddressNorPostalCode_BadRequest() throws Exception {
        String requestBody = "{\"address\":\"\",\"postalCode\":\"\"}";

        mockMvc.perform(post("/api/v1/altitude-temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("address or postalCode must be provided"));
    }

    @Test
    void testGetAltitudeAndTemperature_PostalCodeNotFound_BadRequest() throws Exception {
        // Mock Nominatim response
        MapOsmService.NominatimResult[] nominatimResults = new MapOsmService.NominatimResult[1];
        nominatimResults[0] = createNominatimResult("48.0", "2.0");

        when(restTemplate.exchange(
                contains("nominatim.openstreetmap.org"),
                eq(HttpMethod.GET),
                any(),
                eq(MapOsmService.NominatimResult[].class)
        )).thenReturn(ResponseEntity.ok(nominatimResults));

        // Mock Open-Elevation response
        when(restTemplate.getForObject(
                contains("api.open-elevation.com"),
                eq(MapOsmService.ElevationResponse.class)
        )).thenReturn(createElevationResponse(50.0));

        String requestBody = "{\"address\":\"Paris\",\"postalCode\":\"99\"}"; // 99 doesn't exist in DB

        mockMvc.perform(post("/api/v1/altitude-temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Postal Code")));
    }

    @Test
    void testGetAltitudeAndTemperature_NominatimReturnsEmpty_ReturnZeroAltitude() throws Exception {
        // Mock Nominatim returning empty response
        when(restTemplate.exchange(
                contains("nominatim.openstreetmap.org"),
                eq(HttpMethod.GET),
                any(),
                eq(MapOsmService.NominatimResult[].class)
        )).thenReturn(ResponseEntity.ok(new MapOsmService.NominatimResult[]{}));

        String requestBody = "{\"address\":\"Unknown place xyz abc\",\"postalCode\":\"75\"}";

        mockMvc.perform(post("/api/v1/altitude-temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.altitude").value(0))
                .andExpect(jsonPath("$.standardMinTemperature").value(-5.0)); // base without altitude offset
    }

    @Test
    void testGetAltitudeAndTemperature_WithAltitudeOffset() throws Exception {
        // Test that altitude offset is correctly applied
        // altitude 350m should give offset -1 for Grenoble (base -10.0)
        MapOsmService.NominatimResult[] nominatimResults = new MapOsmService.NominatimResult[1];
        nominatimResults[0] = createNominatimResult("45.2", "5.7");

        when(restTemplate.exchange(
                contains("nominatim.openstreetmap.org"),
                eq(HttpMethod.GET),
                any(),
                eq(MapOsmService.NominatimResult[].class)
        )).thenReturn(ResponseEntity.ok(nominatimResults));

        when(restTemplate.getForObject(
                contains("api.open-elevation.com"),
                eq(MapOsmService.ElevationResponse.class)
        )).thenReturn(createElevationResponse(350.0));

        String requestBody = "{\"address\":\"Grenoble area\",\"postalCode\":\"38\"}";

        mockMvc.perform(post("/api/v1/altitude-temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.altitude").value(350))
                .andExpect(jsonPath("$.standardMinTemperature").value(-11.0)); // -10.0 + (-1)
    }

    @Test
    void testGetAltitudeAndTemperature_OnlyAddressProvided() throws Exception {
        // Test when only address is provided (postalCode can be null)
        MapOsmService.NominatimResult[] nominatimResults = new MapOsmService.NominatimResult[1];
        nominatimResults[0] = createNominatimResult("48.8566", "2.3522");

        when(restTemplate.exchange(
                contains("nominatim.openstreetmap.org"),
                eq(HttpMethod.GET),
                any(),
                eq(MapOsmService.NominatimResult[].class)
        )).thenReturn(ResponseEntity.ok(nominatimResults));

        when(restTemplate.getForObject(
                contains("api.open-elevation.com"),
                eq(MapOsmService.ElevationResponse.class)
        )).thenReturn(createElevationResponse(50.0));

        String requestBody = "{\"address\":\"Paris, France\",\"postalCode\":\"75\"}";

        mockMvc.perform(post("/api/v1/altitude-temp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.altitude").value(50));
    }

    // Helper methods to create mock objects
    private MapOsmService.NominatimResult createNominatimResult(String lat, String lon) {
        MapOsmService.NominatimResult result = new MapOsmService.NominatimResult();
        result.lat = lat;
        result.lon = lon;
        return result;
    }

    private MapOsmService.ElevationResponse createElevationResponse(double elevation) {
        MapOsmService.ElevationResponse response = new MapOsmService.ElevationResponse();
        MapOsmService.ElevationResult result = new MapOsmService.ElevationResult();
        result.elevation = elevation;
        response.results = new MapOsmService.ElevationResult[]{result};
        return response;
    }
}
