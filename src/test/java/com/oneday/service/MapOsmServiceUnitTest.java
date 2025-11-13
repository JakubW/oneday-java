package com.oneday.service;

import com.oneday.config.ApiProperties;
import com.oneday.config.ErrorMessageProperties;
import com.oneday.config.ServiceMessageProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapOsmServiceUnitTest {

    @Test
    void testGetAltitudeMeters_NullAddress_ReturnsZero() {
        ApiProperties apiProps = new ApiProperties();
        ServiceMessageProperties serviceMessages = new ServiceMessageProperties();
        ErrorMessageProperties errorMessages = new ErrorMessageProperties();
        MapOsmService service = new MapOsmService(null, apiProps, serviceMessages, errorMessages);
        int result = service.getAltitudeMeters(null);
        assertEquals(0, result);
    }

    @Test
    void testGetAltitudeMeters_BlankAddress_ReturnsZero() {
        ApiProperties apiProps = new ApiProperties();
        ServiceMessageProperties serviceMessages = new ServiceMessageProperties();
        ErrorMessageProperties errorMessages = new ErrorMessageProperties();
        MapOsmService service = new MapOsmService(null, apiProps, serviceMessages, errorMessages);
        int result = service.getAltitudeMeters("   ");
        assertEquals(0, result);
    }
}

