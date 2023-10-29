package com.hacker.meteomaster.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HourlyUnits {
    @JsonProperty("time")
    public String time;
    @JsonProperty("temperature_2m")
    public String temperature;
    @JsonProperty("rain")
    public String rain;
    @JsonProperty("snowfall")
    public String snowfall;
    @JsonProperty("visibility")
    public String visibility;
    @JsonProperty("windspeed_10m")
    public String windspeed;
    @JsonProperty("uv_index")
    public String uvIndex;
}
