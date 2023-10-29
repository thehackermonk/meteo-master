package com.hacker.meteomaster.controller;

import com.hacker.meteomaster.dto.*;
import com.hacker.meteomaster.service.MeteoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SuppressWarnings("unused")
public class MeteoController {

    @Autowired
    private MeteoService meteoService;

    @PostMapping("/location")
    public ResponseEntity<String> setLocation(@RequestParam String location) {
        return meteoService.setLocation(location);
    }

    @GetMapping("/location")
    public ResponseEntity<Location> getLocation() {
        Location location = meteoService.getLocation();
        return (location != null) ? ResponseEntity.ok(location) : ResponseEntity.notFound().build();
    }

    @GetMapping("/now")
    public ResponseEntity<CurrentWeather> getCurrentWeather() {
        CurrentWeather currentWeather = meteoService.getCurrentWeather();
        return (currentWeather != null) ? ResponseEntity.ok(currentWeather) : ResponseEntity.notFound().build();
    }

    @GetMapping("/day")
    public ResponseEntity<List<Day>> getDailyWeather() {
        return ResponseEntity.ok(meteoService.getDailyWeather());
    }

    @GetMapping("/week")
    public ResponseEntity<List<Week>> getWeeklyWeather() {
        return ResponseEntity.ok(meteoService.getWeeklyWeather());
    }

    @GetMapping("/alert")
    public ResponseEntity<String> getAlert() {
        String alert = meteoService.getAlerts();
        return (alert != null) ? ResponseEntity.ok(alert) : ResponseEntity.notFound().build();
    }

    @GetMapping("/condition")
    public ResponseEntity<String> determineCurrentWeatherCondition() {
        return ResponseEntity.ok(meteoService.determineCurrentWeatherCondition());
    }

    @GetMapping("/locationList")
    public ResponseEntity<List<String>> getLocations() {
        List<String> locations = meteoService.getLocations();
        return (!locations.isEmpty()) ? ResponseEntity.ok(locations) : ResponseEntity.notFound().build();
    }

    @GetMapping("/units")
    public ResponseEntity<Unit> getUnits() {
        return ResponseEntity.ok(meteoService.getUnits());
    }

}
