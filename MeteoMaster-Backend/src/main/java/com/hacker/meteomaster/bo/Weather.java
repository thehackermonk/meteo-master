package com.hacker.meteomaster.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Weather {
    @JsonProperty("latitude")
    public double latitude;
    @JsonProperty("longitude")
    public double longitude;
    @JsonProperty("generationtime_ms")
    public double generationTimeMs;
    @JsonProperty("utc_offset_seconds")
    public int utcOffsetSeconds;
    @JsonProperty("timezone")
    public String timezone;
    @JsonProperty("timezone_abbreviation")
    public String timezoneAbbreviation;
    @JsonProperty("elevation")
    public double elevation;
    @JsonProperty("hourly_units")
    public HourlyUnits hourlyUnits;
    @JsonProperty("hourly")
    public Hourly hourly;
    @JsonProperty("daily_units")
    public DailyUnits dailyUnits;
    @JsonProperty("daily")
    public Daily daily;
}
