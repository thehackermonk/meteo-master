package com.hacker.meteomaster.repository;

import com.hacker.meteomaster.entity.HourlyWeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.Optional;

public interface HourlyWeatherRepository extends JpaRepository<HourlyWeatherEntity, Integer> {
    Optional<HourlyWeatherEntity> findByTime(Timestamp currentDateTime);
}