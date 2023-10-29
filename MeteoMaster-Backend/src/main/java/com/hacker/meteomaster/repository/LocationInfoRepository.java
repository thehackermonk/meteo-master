package com.hacker.meteomaster.repository;

import com.hacker.meteomaster.entity.LocationInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationInfoRepository extends JpaRepository<LocationInfoEntity, String> {
}