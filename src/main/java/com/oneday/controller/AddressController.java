package com.oneday.controller;

import com.oneday.service.MapService;
import com.oneday.service.TemperatureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AddressController {

    private final TemperatureService temperatureService;
    private final MapService mapService;

    public AddressController(TemperatureService temperatureService, MapService mapService) {
        this.temperatureService = temperatureService;
        this.mapService = mapService;
    }

    static class AddressRequest {
        public String address;
        public String postalCode;

        public AddressRequest() {}
    }

    @PostMapping("/altitude-temp")
    public ResponseEntity<?> getAltitudeAndTemperature(@RequestBody AddressRequest req) {
        if ((req.address == null || req.address.isBlank()) && (req.postalCode == null || req.postalCode.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "address or postalCode must be provided"));
        }

        try {
            int altitude = mapService.getAltitudeMeters(req.address);
            double temp = temperatureService.getStandardMinTemperature(req.postalCode, req.address);

            Map<String, Object> body = new HashMap<>();
            body.put("altitude", altitude);
            body.put("standardMinTemperature", temp);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "internal error"));
        }
    }
}
