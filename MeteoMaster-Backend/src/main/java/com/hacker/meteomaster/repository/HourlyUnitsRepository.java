package com.hacker.meteomaster.repository;

import com.hacker.meteomaster.entity.HourlyUnitsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HourlyUnitsRepository extends JpaRepository<HourlyUnitsEntity, String> {
}