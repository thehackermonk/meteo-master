package com.hacker.meteomaster.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.meteomaster.bo.LocationBO;
import com.hacker.meteomaster.bo.Weather;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.hacker.meteomaster.constant.DataConstants.*;

@Log4j2
@Service
public class ExternalAPIUtil {

    public Map<String, String> getLocationDetails(String location) {
        location = location.replace(" ", "+");
        String openStreetMapUrl = "https://nominatim.openstreetmap.org/search?q=" + location + "&format=json";
        try {
            URI uri = URI.create(openStreetMapUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream inputStream = connection.getInputStream()) {
                    String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                    List<LocationBO> locationBOList = parseLocationBOListFromJson(json);

                    Map<String, String> locationMap = new HashMap<>();

                    for (LocationBO locationBO : locationBOList) {
                        if (locationBO.getType().equalsIgnoreCase("city")
                                || locationBO.getType().equalsIgnoreCase("town")) {
                            log.info(location + " : " + locationBO.getName());
                            locationMap.put(LOCATION, locationBO.getName());
                            locationMap.put(LATITUDE, locationBO.getLat());
                            locationMap.put(LONGITUDE, locationBO.getLon());
                        }
                    }

                    return locationMap;
                }
            } else {
                log.info("Error: " + responseCode);
            }
            return Collections.emptyMap();

        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Weather getWeatherData(String latitude, String longitude) {

        String openMeteoUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,rain,snowfall,visibility,windspeed_10m,uv_index&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max&timezone=auto";

        try {
            URI uri = URI.create(openMeteoUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(connection.getInputStream(), Weather.class);

            } else {
                log.error(responseCode);
                return null;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

    }

    List<LocationBO> parseLocationBOListFromJson(String locationDetails) {

        if (locationDetails != null && !locationDetails.isEmpty()) {
            List<LocationBO> locationBOList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(locationDetails);

            for (int i = 0; i < jsonArray.length(); i++) {
                if (!jsonArray.isNull(i)) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    LocationBO locationBO = new LocationBO();

                    locationBO.setPlaceId(jsonObject.getInt("place_id"));
                    locationBO.setLicence(jsonObject.getString("licence"));
                    locationBO.setOsmType(jsonObject.getString("osm_type"));
                    locationBO.setOsmId(jsonObject.getInt("osm_id"));
                    locationBO.setLat(jsonObject.getString("lat"));
                    locationBO.setLon(jsonObject.getString("lon"));
                    locationBO.setLClass(jsonObject.getString("class"));
                    locationBO.setType(jsonObject.getString("type"));
                    locationBO.setPlaceRank(jsonObject.getInt("place_rank"));
                    locationBO.setImportance(jsonObject.getDouble("importance"));
                    locationBO.setAddressType(jsonObject.getString("addresstype"));
                    locationBO.setName(jsonObject.getString("name"));
                    locationBO.setDisplayName(jsonObject.getString("display_name"));

                    JSONArray boundingBoxArray = jsonObject.getJSONArray("boundingbox");
                    List<String> boundingBoxList = new ArrayList<>();

                    for (int j = 0; j < boundingBoxArray.length(); j++) {
                        if (!boundingBoxArray.isNull(j)) {
                            boundingBoxList.add(boundingBoxArray.getString(j));
                        }
                    }
                    locationBO.setBoundingBox(boundingBoxList);
                    locationBOList.add(locationBO);
                }
            }

            return locationBOList;
        } else {
            return Collections.emptyList();
        }
    }

}
