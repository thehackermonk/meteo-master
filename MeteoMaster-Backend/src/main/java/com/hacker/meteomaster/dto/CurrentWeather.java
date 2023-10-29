package com.hacker.meteomaster.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class CurrentWeather {
    private double currentTemperature;
    private double high;
    private double low;
    private String timezone;
    private LocalTime sunrise;
    private LocalTime sunset;
    private double rain;
    private double snowfall;
    private int uv;
    private String uvCondition;
    private double wind;
    private int visibility;
}
