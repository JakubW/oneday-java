package com.oneday.bootstrap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Component
public class DataLoader implements CommandLineRunner {

    private final PostalTemperatureRepository repository;
    private final AltitudeOffsetRangeRepository offsetRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(DataLoader.class);

    public DataLoader(PostalTemperatureRepository repository, AltitudeOffsetRangeRepository offsetRepository) {
        this.repository = repository;
        this.offsetRepository = offsetRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading temperature dataset into DB...");
        ClassPathResource resource = new ClassPathResource("datasets/temperatures.json");
        try (InputStream is = resource.getInputStream()) {
            List<PostalTemperature> list = mapper.readValue(is, new TypeReference<List<PostalTemperature>>(){});
            repository.saveAll(list);
            log.info("Loaded {} postal temperatures.", list.size());
        } catch (Exception e) {
            log.error("Failed to load temperatures.json", e);
        }

        log.info("Loading offsets dataset into DB...");
        ClassPathResource offRes = new ClassPathResource("datasets/offsets.json");
        try (InputStream is = offRes.getInputStream()) {
            List<AltitudeOffsetRange> offsets = mapper.readValue(is, new TypeReference<List<AltitudeOffsetRange>>(){});
            offsetRepository.saveAll(offsets);
            log.info("Loaded {} offset ranges.", offsets.size());
        } catch (Exception e) {
            log.error("Failed to load offsets.json", e);
        }
    }
}