package com.hacker.meteomaster.controller;

import com.hacker.meteomaster.bo.Weather;
import com.hacker.meteomaster.service.MeteoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.hacker.meteomaster.constant.DataConstants.FAILED_TO_LOAD;
import static com.hacker.meteomaster.constant.DataConstants.INITIAL_DATA_LOADED;

@RestController
@SuppressWarnings("unused")
public class InitializeController {

    @Autowired
    private MeteoService meteoService;

    @PostMapping("/initializeTestData")
    public ResponseEntity<String> initializeTestData(@RequestBody Weather weatherBo) {
        if (meteoService.insertWeatherAndLocationToDB(weatherBo, "Thiruvananthapuram"))
            return ResponseEntity.ok(INITIAL_DATA_LOADED);
        else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FAILED_TO_LOAD);
        }
    }

    @GetMapping("/validateLocations")
    public ResponseEntity<List<String>> getValidatedLocations() {
        List<String> validatedLocations = meteoService.validatedLocations();
        if (!validatedLocations.isEmpty())
            return ResponseEntity.ok(validatedLocations);
        else
            return ResponseEntity.notFound().build();
    }

}
