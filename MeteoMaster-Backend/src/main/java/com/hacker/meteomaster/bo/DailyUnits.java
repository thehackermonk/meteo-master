package com.hacker.meteomaster.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyUnits {
    @JsonProperty("time")
    public String time;
    @JsonProperty("temperature_2m_max")
    public String temperatureMax;
    @JsonProperty("temperature_2m_min")
    public String temperatureMin;
    @JsonProperty("sunrise")
    public String sunrise;
    @JsonProperty("sunset")
    public String sunset;
    @JsonProperty("uv_index_max")
    public String uvIndexMax;
}
