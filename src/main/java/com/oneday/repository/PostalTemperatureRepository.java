package com.oneday.repository;

import com.oneday.model.PostalTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostalTemperatureRepository extends JpaRepository<PostalTemperature, String> {
}

