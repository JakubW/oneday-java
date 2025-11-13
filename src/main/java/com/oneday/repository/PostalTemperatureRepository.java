package com.oneday.repository;

import com.oneday.model.PostalTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for PostalTemperature entities.
 * Provides database operations for postal temperature data (base temperatures for French postal codes).
 */
@Repository
public interface PostalTemperatureRepository extends JpaRepository<PostalTemperature, String> {
}

