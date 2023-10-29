package com.hacker.meteomaster.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Hourly {
    @JsonProperty("time")
    public List<String> time;
    @JsonProperty("temperature_2m")
    public List<Double> temperature;
    @JsonProperty("rain")
    public List<Double> rain;
    @JsonProperty("snowfall")
    public List<Double> snowfall;
    @JsonProperty("visibility")
    public List<Double> visibility;
    @JsonProperty("windspeed_10m")
    public List<Double> windspeed;
    @JsonProperty("uv_index")
    public List<Double> uvIndex;
}
