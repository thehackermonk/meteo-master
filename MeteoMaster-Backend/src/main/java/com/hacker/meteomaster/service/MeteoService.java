package com.hacker.meteomaster.service;

import com.hacker.meteomaster.bo.Weather;
import com.hacker.meteomaster.dto.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MeteoService {
    ResponseEntity<String> setLocation(String location);

    Location getLocation();

    CurrentWeather getCurrentWeather();

    List<Day> getDailyWeather();

    List<Week> getWeeklyWeather();

    String determineCurrentWeatherCondition();

    String getAlerts();

    boolean insertWeatherAndLocationToDB(Weather weather, String location);

    List<String> getLocations();

    Unit getUnits();

    List<String> validatedLocations();
}
