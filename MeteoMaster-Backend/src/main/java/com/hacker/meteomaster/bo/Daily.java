package com.hacker.meteomaster.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Daily {
    @JsonProperty("time")
    public List<String> time;
    @JsonProperty("temperature_2m_max")
    public List<Double> temperatureMax;
    @JsonProperty("temperature_2m_min")
    public List<Double> temperatureMin;
    @JsonProperty("sunrise")
    public List<String> sunrise;
    @JsonProperty("sunset")
    public List<String> sunset;
    @JsonProperty("uv_index_max")
    public List<Double> uvIndexMax;
}
