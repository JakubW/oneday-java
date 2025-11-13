package com.oneday.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.model.AltitudeOffsetRange;
import com.oneday.model.PostalTemperature;
import com.oneday.repository.AltitudeOffsetRangeRepository;
import com.oneday.repository.PostalTemperatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Bootstrap component that loads initial data from JSON files into the database.
 * Clears existing data before loading to ensure fresh state.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final PostalTemperatureRepository repository;
    private final AltitudeOffsetRangeRepository offsetRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Value("${app.datasets.temperatures}")
    private String temperaturesDatasetPath;

    @Value("${app.datasets.offsets}")
    private String offsetsDatasetPath;

    public DataLoader(PostalTemperatureRepository repository, AltitudeOffsetRangeRepository offsetRepository) {
        this.repository = repository;
        this.offsetRepository = offsetRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadTemperatureData();
        loadOffsetData();
    }

    /**
     * Load postal temperature data from JSON file.
     * Clears existing data before loading to prevent duplicates.
     */
    private void loadTemperatureData() {
        log.info("Loading temperature dataset into DB...");
        try {
            long existingCount = repository.count();
            if (existingCount > 0) {
                log.info("Clearing {} existing postal temperature records", existingCount);
                repository.deleteAll();
            }

            ClassPathResource resource = new ClassPathResource(temperaturesDatasetPath);
            try (InputStream is = resource.getInputStream()) {
                List<PostalTemperature> list = mapper.readValue(is, new TypeReference<List<PostalTemperature>>(){});
                repository.saveAll(list);
                log.info("Successfully loaded {} postal temperatures.", list.size());
            }
        } catch (Exception e) {
            log.error("Failed to load temperatures.json", e);
        }
    }

    /**
     * Load altitude offset range data from JSON file.
     * Clears existing data before loading to prevent duplicates.
     */
    private void loadOffsetData() {
        log.info("Loading altitude offset dataset into DB...");
        try {
            long existingCount = offsetRepository.count();
            if (existingCount > 0) {
                log.info("Clearing {} existing altitude offset records", existingCount);
                offsetRepository.deleteAll();
            }

            ClassPathResource offRes = new ClassPathResource(offsetsDatasetPath);
            try (InputStream is = offRes.getInputStream()) {
                List<AltitudeOffsetRange> offsets = mapper.readValue(is, new TypeReference<List<AltitudeOffsetRange>>(){});
                offsetRepository.saveAll(offsets);
                log.info("Successfully loaded {} altitude offset ranges.", offsets.size());
            }
        } catch (Exception e) {
            log.error("Failed to load offsets.json", e);
        }
    }
}