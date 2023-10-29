package com.hacker.meteomaster.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class Week {
    private LocalDate date;
    private double high;
    private double low;
    private LocalTime sunrise;
    private LocalTime sunset;
    private double uv;
}
