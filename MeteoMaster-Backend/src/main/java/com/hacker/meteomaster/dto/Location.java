package com.hacker.meteomaster.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Location {
    private String locationName;
    private double latitude;
    private double longitude;
    private double elevation;
    private String timezone;
    private String timezoneAbbreviation;
    private LocalDateTime currentTime;
}
