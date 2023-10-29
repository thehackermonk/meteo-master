package com.hacker.meteomaster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    private String temperatureMin;
    private String temperatureMax;
    private String windspeed;
    private String visibility;
    private String snowfall;
    private String rain;
    private String temperature;

    public Unit(HourlyUnits hourlyUnits, DailyUnits dailyUnits) {
        temperatureMin = dailyUnits.getTemperatureMin();
        temperatureMax = dailyUnits.getTemperatureMax();
        windspeed = hourlyUnits.getWindspeed();
        visibility = hourlyUnits.getVisibility();
        snowfall = hourlyUnits.getSnowfall();
        rain = hourlyUnits.getRain();
        temperature = hourlyUnits.getTemperature();
    }

}
