package com.hacker.meteomaster.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_weather")
public class DailyWeatherEntity {
    @Id
    @Column(name = "daily_weather_id", nullable = false)
    private Integer dailyWeatherId;
    @Column(nullable = false)
    private Date time;
    @Column(name = "temperature_max", nullable = false)
    private double temperatureMax;
    @Column(name = "temperature_min", nullable = false)
    private double temperatureMin;
    @Column(nullable = false)
    private Timestamp sunrise;
    @Column(nullable = false)
    private Timestamp sunset;
    @Column(name = "uv_index_max", nullable = false)
    private double uvIndexMax;
}