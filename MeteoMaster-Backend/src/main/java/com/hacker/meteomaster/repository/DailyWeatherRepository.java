package com.hacker.meteomaster.repository;

import com.hacker.meteomaster.entity.DailyWeatherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.Optional;

public interface DailyWeatherRepository extends JpaRepository<DailyWeatherEntity, Integer> {
    Optional<DailyWeatherEntity> findByTime(Date currentDate);
}