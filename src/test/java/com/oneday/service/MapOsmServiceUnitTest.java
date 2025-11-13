package com.oneday.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapOsmServiceUnitTest {

    @Test
    void testGetAltitudeMeters_NullAddress_ReturnsZero() {
        MapOsmService service = new MapOsmService(null); // no restTemplate needed for null test
        int result = service.getAltitudeMeters(null);
        assertEquals(0, result);
    }

    @Test
    void testGetAltitudeMeters_BlankAddress_ReturnsZero() {
        MapOsmService service = new MapOsmService(null);
        int result = service.getAltitudeMeters("   ");
        assertEquals(0, result);
    }
}

