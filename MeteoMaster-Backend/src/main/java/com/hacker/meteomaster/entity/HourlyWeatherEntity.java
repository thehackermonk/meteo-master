package com.hacker.meteomaster.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hourly_weather")
public class HourlyWeatherEntity {
    @Id
    @Column(name = "hourly_weather_id", nullable = false)
    private Integer hourlyWeatherId;
    @Column(nullable = false)
    private Timestamp time;
    @Column(nullable = false)
    private double temperature;
    @Column(nullable = false)
    private double rain;
    @Column(nullable = false)
    private double snowfall;
    @Column(nullable = false)
    private double visibility;
    @Column(name = "windspeed", nullable = false)
    private double windSpeed;
    @Column(name = "uv_index", nullable = false)
    private double uvIndex;
}