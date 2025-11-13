package com.oneday.service;

import com.oneday.config.ErrorMessageProperties;
import com.oneday.config.ServiceMessageProperties;
import com.oneday.model.AltitudeOffsetRange;
import com.oneday.model.PostalTemperature;
import com.oneday.repository.AltitudeOffsetRangeRepository;
import com.oneday.repository.PostalTemperatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemperatureServiceUnitTest {

    @Mock
    private PostalTemperatureRepository repository;

    @Mock
    private MapService mapService;

    @Mock
    private AltitudeOffsetRangeRepository altitudeOffsetRangeRepository;

    @Mock
    private ErrorMessageProperties errorMessages;

    @Mock
    private ServiceMessageProperties serviceMessages;

    private TemperatureService temperatureService;

    @BeforeEach
    void setup() {
        // Setup service message properties mocks
        ServiceMessageProperties.Temperature temperatureProps = new ServiceMessageProperties.Temperature();
        temperatureProps.setNoOffsets("No altitude offset ranges configured, returning 0");
        temperatureProps.setAltitudeExceeds("Altitude {0} exceeds maximum configured altitude {1}");

        lenient().when(serviceMessages.getTemperature()).thenReturn(temperatureProps);
        lenient().when(errorMessages.getPostalCodeNotFound()).thenReturn("Postal Code prefix not found in temperature data or vice versa.");
        lenient().when(errorMessages.getAltitudeExceed()).thenReturn("Altitude exceed {0} meters, no temperature offset data available.");

        temperatureService = new TemperatureService(repository, mapService, altitudeOffsetRangeRepository, errorMessages, serviceMessages);

        // default offset ranges matching datasets/offsets.json
        lenient().when(altitudeOffsetRangeRepository.findAllByOrderByFromMetersAsc()).thenReturn(Arrays.asList(
                new AltitudeOffsetRange(-10000, 0, 2),
                new AltitudeOffsetRange(0, 199, 0),
                new AltitudeOffsetRange(200, 399, -1),
                new AltitudeOffsetRange(400, 599, -2),
                new AltitudeOffsetRange(600, 799, -3),
                new AltitudeOffsetRange(800, 999, -4),
                new AltitudeOffsetRange(1000, 1200, -5)
        ));
    }

    @Test
    void testGetStandardMinTemperature_AltitudeAbove1200_ThrowsIllegalArgumentException() {
        // Arrange
        when(repository.findById("75")).thenReturn(Optional.of(new PostalTemperature("75", -5.0)));
        when(mapService.getAltitudeMeters(anyString())).thenReturn(1201); // > 1200

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> temperatureService.getStandardMinTemperature("75", "Paris"));
    }

    @Test
    void testGetStandardMinTemperature_AltitudeExactly1200_DoesNotThrow() {
        // Arrange
        when(repository.findById("75")).thenReturn(Optional.of(new PostalTemperature("75", -5.0)));
        when(mapService.getAltitudeMeters(anyString())).thenReturn(1200); // exactly 1200, should not throw

        // Act
        double result = temperatureService.getStandardMinTemperature("75", "Paris");

        // Assert
        assertEquals(-10.0, result); // -5.0 + offset(-5) for 1000+ meters
    }

    @Test
    void testGetStandardMinTemperature_PostalCodeNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        when(repository.findById("99")).thenReturn(Optional.empty()); // Not found
        lenient().when(mapService.getAltitudeMeters(anyString())).thenReturn(100);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> temperatureService.getStandardMinTemperature("99", "Unknown"));
    }

    @Test
    void testGetStandardMinTemperature_WithValidAltitudeOffset() {
        // Arrange - altitude 350m gives offset -1
        when(repository.findById("38")).thenReturn(Optional.of(new PostalTemperature("38", -10.0)));
        when(mapService.getAltitudeMeters(anyString())).thenReturn(350);

        // Act
        double result = temperatureService.getStandardMinTemperature("38", "Grenoble");

        // Assert
        assertEquals(-11.0, result); // -10.0 + (-1)
    }

    @Test
    void testGetStandardMinTemperature_AltitudeZero() {
        // Arrange
        when(repository.findById("75")).thenReturn(Optional.of(new PostalTemperature("75", -5.0)));
        when(mapService.getAltitudeMeters(anyString())).thenReturn(0);

        // Act
        double result = temperatureService.getStandardMinTemperature("75", "Paris");

        // Assert
        assertEquals(-3.0, result); // -5.0 + 2 offset (two ranges of 0)
    }

    @Test
    void testGetStandardMinTemperature_NegativeAltitude() {
        // Arrange - negative altitude gives offset +2
        when(repository.findById("75")).thenReturn(Optional.of(new PostalTemperature("75", -5.0)));
        when(mapService.getAltitudeMeters(anyString())).thenReturn(-100);

        // Act
        double result = temperatureService.getStandardMinTemperature("75", "Paris");

        // Assert
        assertEquals(-3.0, result); // -5.0 + 2
    }
}
