package com.oneday.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneday.config.DatasetProperties;
import com.oneday.model.AltitudeOffsetRange;
import com.oneday.model.PostalTemperature;
import com.oneday.repository.AltitudeOffsetRangeRepository;
import com.oneday.repository.PostalTemperatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final DatasetProperties datasetProperties;

    public DataLoader(PostalTemperatureRepository repository, AltitudeOffsetRangeRepository offsetRepository,
                      DatasetProperties datasetProperties) {
        this.repository = repository;
        this.offsetRepository = offsetRepository;
        this.datasetProperties = datasetProperties;
    }

    @Override
    public void run(String... args) {
        loadTemperatureData();
        loadOffsetData();
    }

    /**
     * Load postal temperature data from JSON file.
     * Clears existing data before loading to prevent duplicates.
     */
    private void loadTemperatureData() {
        loadDatasetFromFile(
            datasetProperties.getTemperatures(),
            repository,
            "postal temperatures",
            new TypeReference<List<PostalTemperature>>() {}
        );
    }

    /**
     * Load altitude offset range data from JSON file.
     * Clears existing data before loading to prevent duplicates.
     */
    private void loadOffsetData() {
        loadDatasetFromFile(
            datasetProperties.getOffsets(),
            offsetRepository,
            "altitude offset ranges",
            new TypeReference<List<AltitudeOffsetRange>>() {}
        );
    }

    /**
     * Generic method to load dataset from JSON file into repository.
     * Clears existing data before loading to prevent duplicates.
     *
     * @param filePath the classpath resource file path
     * @param repository the Spring Data repository to save data to
     * @param datasetName human-readable name of the dataset
     * @param typeReference Jackson TypeReference for JSON deserialization
     * @param <T> the type of entity being loaded
     */
    private <T> void loadDatasetFromFile(
        String filePath,
        org.springframework.data.repository.CrudRepository<T, ?> repository,
        String datasetName,
        TypeReference<List<T>> typeReference) {

        log.info("Loading {} dataset into DB...", datasetName);
        try {
            clearExistingData(repository, datasetName);
            loadDataFromJsonFile(filePath, repository, datasetName, typeReference);
        } catch (Exception e) {
            log.error("Failed to load {}.json", datasetName, e);
        }
    }

    /**
     * Clear existing data from repository if any exists.
     */
    private <T> void clearExistingData(
        org.springframework.data.repository.CrudRepository<T, ?> repository,
        String datasetName) {

        long existingCount = repository.count();
        if (existingCount > 0) {
            log.info("Clearing {} existing {} records", existingCount, datasetName);
            repository.deleteAll();
        }
    }

    /**
     * Load data from JSON file and save to repository.
     */
    private <T> void loadDataFromJsonFile(
        String filePath,
        org.springframework.data.repository.CrudRepository<T, ?> repository,
        String datasetName,
        TypeReference<List<T>> typeReference) {

        ClassPathResource resource = new ClassPathResource(filePath);
        try (InputStream inputStream = resource.getInputStream()) {
            List<T> dataList = mapper.readValue(inputStream, typeReference);
            repository.saveAll(dataList);
            log.info("Successfully loaded {} {}.", dataList.size(), datasetName);
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Failed to load data from file: %s", filePath), e
            );
        }
    }
}