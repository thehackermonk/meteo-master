package com.hacker.meteomaster.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class Day {
    private LocalTime time;
    private double temperature;
    private double rain;
    private double snowfall;
    private int uv;
    private double wind;
    private int visibility;
}
