package com.hacker.meteomaster.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LocationBO {
    @JsonProperty("place_id")
    public Integer placeId;
    @JsonProperty("licence")
    public String licence;
    @JsonProperty("osm_type")
    public String osmType;
    @JsonProperty("osm_id")
    public Integer osmId;
    @JsonProperty("lat")
    public String lat;
    @JsonProperty("lon")
    public String lon;
    @JsonProperty("class")
    public String lClass;
    @JsonProperty("type")
    public String type;
    @JsonProperty("place_rank")
    public Integer placeRank;
    @JsonProperty("importance")
    public Double importance;
    @JsonProperty("addresstype")
    public String addressType;
    @JsonProperty("name")
    public String name;
    @JsonProperty("display_name")
    public String displayName;
    @JsonProperty("boundingbox")
    public List<String> boundingBox;
}
