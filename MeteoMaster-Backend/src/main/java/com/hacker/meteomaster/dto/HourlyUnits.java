package com.hacker.meteomaster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HourlyUnits {
    private String windspeed;
    private String visibility;
    private String snowfall;
    private String rain;
    private String temperature;
}
