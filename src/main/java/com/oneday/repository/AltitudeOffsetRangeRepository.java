package com.oneday.repository;

import com.oneday.model.AltitudeOffsetRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AltitudeOffsetRangeRepository extends JpaRepository<AltitudeOffsetRange, Long> {
    List<AltitudeOffsetRange> findAllByOrderByFromMetersAsc();
}