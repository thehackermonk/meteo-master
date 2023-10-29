package com.hacker.meteomaster.repository;

import com.hacker.meteomaster.entity.DailyUnitsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyUnitsRepository extends JpaRepository<DailyUnitsEntity, String> {
}