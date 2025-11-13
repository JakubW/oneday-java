package com.oneday.controller;

import com.oneday.dto.AddressRequest;
import com.oneday.dto.AltitudeTemperatureResponse;
import com.oneday.service.MapService;
import com.oneday.service.TemperatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for address-based altitude and temperature queries.
 */
@RestController
@RequestMapping("/api/v1")
public class AddressController {

    private final TemperatureService temperatureService;
    private final MapService mapService;

    public AddressController(TemperatureService temperatureService, MapService mapService) {
        this.temperatureService = temperatureService;
        this.mapService = mapService;
    }

    /**
     * Get altitude and standard minimum temperature for a given address and postal code.
     *
     * @param request DTO containing address and postalCode
     * @return Response with altitude in meters and temperature in Celsius
     */
    @PostMapping("/altitude-temp")
    public ResponseEntity<AltitudeTemperatureResponse> getAltitudeAndTemperature(@Valid @RequestBody AddressRequest request) {
        int altitude;
        try {
            altitude = mapService.getAltitudeMeters(request.getAddress());
        } catch (IllegalArgumentException e) {
            // If address cannot be geocoded, return 0 altitude
            altitude = 0;
        }

        double temp = temperatureService.getStandardMinTemperature(request.getPostalCode(), request.getAddress());

        AltitudeTemperatureResponse response = new AltitudeTemperatureResponse(altitude, temp);
        return ResponseEntity.ok(response);
    }
}


