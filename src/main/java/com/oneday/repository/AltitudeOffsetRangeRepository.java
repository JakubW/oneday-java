package com.oneday.repository;

import com.oneday.model.AltitudeOffsetRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AltitudeOffsetRange entities.
 * Provides database operations for altitude-based temperature offset data.
 */
@Repository
public interface AltitudeOffsetRangeRepository extends JpaRepository<AltitudeOffsetRange, Long> {
    /**
     * Find all altitude offset ranges sorted by starting altitude (fromMeters) in ascending order.
     *
     * @return list of altitude offset ranges sorted by fromMeters
     */
    List<AltitudeOffsetRange> findAllByOrderByFromMetersAsc();
}