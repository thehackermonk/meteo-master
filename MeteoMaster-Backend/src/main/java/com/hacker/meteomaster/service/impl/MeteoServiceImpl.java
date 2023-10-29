package com.hacker.meteomaster.service.impl;

import com.hacker.meteomaster.bo.Weather;
import com.hacker.meteomaster.dto.*;
import com.hacker.meteomaster.entity.*;
import com.hacker.meteomaster.repository.*;
import com.hacker.meteomaster.service.MeteoService;
import com.hacker.meteomaster.util.ExternalAPIUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.hacker.meteomaster.constant.DataConstants.*;

@Service
@SuppressWarnings("unused")
@Log4j2
public class MeteoServiceImpl implements MeteoService {

    @Autowired
    private DailyUnitsRepository dailyUnitsRepository;
    @Autowired
    private DailyWeatherRepository dailyWeatherRepository;
    @Autowired
    private HourlyUnitsRepository hourlyUnitsRepository;
    @Autowired
    private HourlyWeatherRepository hourlyWeatherRepository;
    @Autowired
    private LocationInfoRepository locationInfoRepository;

    @Autowired
    private ExternalAPIUtil externalAPIUtil;

    @Override
    public ResponseEntity<String> setLocation(String location) {

        List<LocationInfoEntity> locationInfoEntities = locationInfoRepository.findAll();

        boolean allowed = true;
        if (!locationInfoEntities.isEmpty()) {
            LocationInfoEntity locationInfoEntity = locationInfoEntities.get(0);
            LocalDate lastUpdated = locationInfoEntity.getLastLocationChange().toLocalDate();
            log.info("last updated date: " + lastUpdated);
            if (!lastUpdated.isBefore(LocalDate.now())) {
                allowed = false;
            }
        }

        if (allowed) {
            log.info("allowed");
            Map<String, String> locationDetail = externalAPIUtil.getLocationDetails(location);
            log.info("location: " + locationDetail.isEmpty());
            log.info("location: " + locationDetail.size());
            if (!locationDetail.isEmpty()) {
                Weather weather = externalAPIUtil.getWeatherData(locationDetail.get(LATITUDE), locationDetail.get(LONGITUDE));
                if (insertWeatherAndLocationToDB(weather, locationDetail.get(LOCATION)))
                    return ResponseEntity.ok(LOCATION_SET_SUCCESS);
                else
                    return new ResponseEntity<>(LOCATION_UPDATE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<>(LOCATION_FETCH_ERROR, HttpStatus.TOO_MANY_REQUESTS);
            }
        } else {
            return new ResponseEntity<>(LOCATION_UPDATE_NOT_ALLOWED, HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Override
    public Location getLocation() {

        List<LocationInfoEntity> locationInfoEntities = locationInfoRepository.findAll();

        if (locationInfoEntities.isEmpty()) {
            return null;
        } else {
            Location location = new Location();

            location.setLocationName(locationInfoEntities.get(0).getLocation());
            location.setLatitude(locationInfoEntities.get(0).getLatitude());
            location.setLongitude(locationInfoEntities.get(0).getLongitude());
            location.setElevation(locationInfoEntities.get(0).getElevation());
            location.setTimezone(locationInfoEntities.get(0).getTimezone());
            location.setTimezoneAbbreviation(locationInfoEntities.get(0).getTimezoneAbbreviation());
            location.setCurrentTime(LocalDateTime.now());

            return location;
        }
    }

    @Override
    public CurrentWeather getCurrentWeather() {
        CurrentWeather currentWeather = new CurrentWeather();

        Timestamp currentDateTime = Timestamp.valueOf(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
        Date currentDate = Date.valueOf(LocalDate.now());

        log.info(currentDateTime);

        refreshData();

        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = hourlyWeatherRepository.findByTime(currentDateTime);
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = dailyWeatherRepository.findByTime(currentDate);
        List<LocationInfoEntity> locationInfoEntities = locationInfoRepository.findAll();

        if (locationInfoEntities.isEmpty())
            return null;
        else {

            LocationInfoEntity locationInfoEntity = locationInfoEntities.get(0);

            currentWeather.setTimezone(locationInfoEntity.getTimezone());

            if (optionalHourlyWeatherEntity.isPresent()) {
                HourlyWeatherEntity hourlyWeatherEntity = optionalHourlyWeatherEntity.get();
                currentWeather.setCurrentTemperature(hourlyWeatherEntity.getTemperature());
                currentWeather.setRain(hourlyWeatherEntity.getRain());
                currentWeather.setSnowfall(hourlyWeatherEntity.getSnowfall());
                int uvIndex = (int) hourlyWeatherEntity.getUvIndex();
                currentWeather.setUv(uvIndex);
                currentWeather.setUvCondition(getUVCondition(uvIndex));
                currentWeather.setWind(hourlyWeatherEntity.getWindSpeed());
                currentWeather.setVisibility((int) hourlyWeatherEntity.getVisibility());

            }

            if (optionalDailyWeatherEntity.isPresent()) {
                DailyWeatherEntity dailyWeatherEntity = optionalDailyWeatherEntity.get();
                currentWeather.setHigh(dailyWeatherEntity.getTemperatureMax());
                currentWeather.setLow(dailyWeatherEntity.getTemperatureMin());
                log.info(dailyWeatherEntity.getSunrise());
                log.info(dailyWeatherEntity.getSunrise().toLocalDateTime());
                log.info(dailyWeatherEntity.getSunrise().toLocalDateTime().toLocalTime());
                currentWeather.setSunrise(dailyWeatherEntity.getSunrise().toLocalDateTime().toLocalTime());
                currentWeather.setSunset(dailyWeatherEntity.getSunset().toLocalDateTime().toLocalTime());

            }
        }

        return currentWeather;

    }

    @Override
    public List<Day> getDailyWeather() {

        refreshData();

        List<LocationInfoEntity> locationInfoEntities = locationInfoRepository.findAll();

        if (locationInfoEntities.isEmpty())
            return Collections.emptyList();
        else {
            List<Day> days = new ArrayList<>();

            LocalDateTime dateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

            for (int i = 0; i < 24; i++) {
                Timestamp timestamp = Timestamp.valueOf(dateTime);
                log.info(i + ": " + timestamp);
                Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = hourlyWeatherRepository.findByTime(timestamp);
                if (optionalHourlyWeatherEntity.isPresent()) {
                    HourlyWeatherEntity hourlyWeatherEntity = optionalHourlyWeatherEntity.get();
                    Day day = new Day();

                    day.setTime(timestamp.toLocalDateTime().toLocalTime());
                    day.setTemperature(hourlyWeatherEntity.getTemperature());
                    day.setRain(hourlyWeatherEntity.getRain());
                    day.setSnowfall(hourlyWeatherEntity.getSnowfall());
                    day.setUv((int) hourlyWeatherEntity.getUvIndex());
                    day.setWind(hourlyWeatherEntity.getWindSpeed());
                    day.setVisibility((int) hourlyWeatherEntity.getVisibility());

                    days.add(day);
                }
                dateTime = dateTime.plusHours(1);
            }

            return days;
        }
    }

    @Override
    public List<Week> getWeeklyWeather() {
        refreshData();
        List<LocationInfoEntity> locationInfoEntities = locationInfoRepository.findAll();

        if (locationInfoEntities.isEmpty())
            return Collections.emptyList();
        else {
            List<Week> weeks = new ArrayList<>();

            for (int i = 0; i < 7; i++) {
                LocalDate localDate = LocalDate.now().plusDays(i);
                Date date = Date.valueOf(localDate);
                log.info(i + ": " + date);

                Optional<DailyWeatherEntity> optionalDailyWeatherEntity = dailyWeatherRepository.findByTime(date);
                if (optionalDailyWeatherEntity.isPresent()) {
                    DailyWeatherEntity dailyWeatherEntity = optionalDailyWeatherEntity.get();
                    Week week = new Week();

                    week.setDate(localDate);
                    week.setHigh(dailyWeatherEntity.getTemperatureMax());
                    week.setLow(dailyWeatherEntity.getTemperatureMin());
                    week.setSunrise(dailyWeatherEntity.getSunrise().toLocalDateTime().toLocalTime());
                    week.setSunset(dailyWeatherEntity.getSunset().toLocalDateTime().toLocalTime());
                    week.setUv(dailyWeatherEntity.getUvIndexMax());

                    weeks.add(week);
                }
            }

            return weeks;
        }
    }

    CurrentWeather getWeatherAfterTwoHours() {
        CurrentWeather currentWeather = new CurrentWeather();

        Timestamp currentDateTime = Timestamp.valueOf(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(2));
        Date currentDate = Date.valueOf(LocalDate.now());

        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = hourlyWeatherRepository.findByTime(currentDateTime);

        if (optionalHourlyWeatherEntity.isPresent()) {
            HourlyWeatherEntity hourlyWeatherEntity = optionalHourlyWeatherEntity.get();
            currentWeather.setCurrentTemperature(hourlyWeatherEntity.getTemperature());
            currentWeather.setRain(hourlyWeatherEntity.getRain());
            currentWeather.setSnowfall(hourlyWeatherEntity.getSnowfall());
            currentWeather.setUv((int) hourlyWeatherEntity.getUvIndex());
            currentWeather.setWind(hourlyWeatherEntity.getWindSpeed());
            currentWeather.setVisibility((int) hourlyWeatherEntity.getVisibility());

        }

        return currentWeather;

    }

    void refreshData() {
        List<LocationInfoEntity> locationInfoEntities = locationInfoRepository.findAll();
        if (!locationInfoEntities.isEmpty()) {
            LocationInfoEntity locationInfoEntity = locationInfoEntities.get(0);
            LocalDate lastUpdated = locationInfoEntity.getLastDataRefresh().toLocalDate();
            log.info("last updated date: " + lastUpdated);
            if (lastUpdated.isBefore(LocalDate.now())) {
                log.info("allowed");
                Weather weather = externalAPIUtil.getWeatherData(String.valueOf(locationInfoEntity.getLatitude()), String.valueOf(locationInfoEntity.getLongitude()));
                insertWeatherToDB(weather);
            }
        }
    }

    @Override
    public String determineCurrentWeatherCondition() {

        CurrentWeather currentWeather = getCurrentWeather();

        if (currentWeather.getCurrentTemperature() > 20 && currentWeather.getWind() > 20) {
            return CLOUDY_WINDY;
        } else if (currentWeather.getCurrentTemperature() > 20) {
            return CLOUDY;
        } else if (currentWeather.getCurrentTemperature() > 10) {
            return SUNNY;
        } else if (currentWeather.getVisibility() < 100) {
            return FOG;
        } else if (currentWeather.getUv() >= 8) {
            return HAIL;
        } else if (currentWeather.getRain() > 0 && currentWeather.getRain() < 5) {
            return RAIN;
        } else if (currentWeather.getRain() >= 5) {
            return SHOWERS;
        } else if (currentWeather.getSnowfall() > 0) {
            return SNOW;
        } else if (currentWeather.getWind() > 30) {
            return STORM_SHOWERS;
        } else if (currentWeather.getWind() > 10) {
            return WINDY;
        } else {
            return HUMIDITY;
        }
    }

    @Override
    public String getAlerts() {
        CurrentWeather weather = getWeatherAfterTwoHours();

        if (weather.getSnowfall() > 15 && weather.getVisibility() < 200) {
            return BLIZZARD_ALERT;
        } else if (weather.getRain() > 50 && weather.getWind() > 70) {
            return THUNDERSTORM_ALERT;
        } else if (weather.getUv() > 7) {
            return HIGH_SUNBURN_ALERT;
        } else if (weather.getRain() > 50) {
            return HEAVY_RAINFALL_ALERT;
        } else if (weather.getSnowfall() > 10) {
            return HEAVY_SNOW_ALERT;
        } else if (weather.getWind() > 60) {
            return WINDSTORM_ALERT;
        } else if (weather.getVisibility() < 100) {
            return DENSE_FOG_ALERT;
        } else if (weather.getCurrentTemperature() > 35) {
            return EXTREME_HEAT_ALERT;
        } else if (weather.getCurrentTemperature() < -20) {
            return EXTREME_COLD_ALERT;
        } else {
            return null;
        }
    }

    String getUVCondition(int uvIndex) {
        if (uvIndex >= 0 && uvIndex <= 2) {
            return LOW;
        } else if (uvIndex >= 3 && uvIndex <= 5) {
            return MODERATE;
        } else if (uvIndex >= 6 && uvIndex <= 7) {
            return HIGH;
        } else if (uvIndex >= 8 && uvIndex <= 10) {
            return VERY_HIGH;
        } else if (uvIndex >= 11) {
            return EXTREME;
        } else {
            log.error("Invalid UV Index");
            return null;
        }
    }

    void clearAllData() {
        try {
            locationInfoRepository.deleteAll();
            clearLocationData();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    void clearLocationData() {
        try {
            dailyUnitsRepository.deleteAll();
            dailyWeatherRepository.deleteAll();
            hourlyUnitsRepository.deleteAll();
            hourlyWeatherRepository.deleteAll();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    @Override
    public boolean insertWeatherAndLocationToDB(Weather weather, String location) {

        weather.hourly.temperature.forEach(log::info);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            clearAllData();
            insertLocationInfo(location, weather);
            insertHourlyUnits(weather);
            insertHourlyWeather(weather, formatter);
            insertDailyUnits(weather);
            insertDailyWeather(weather, formatter);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getLocations() {
        try {
            Resource resource = new ClassPathResource("locations.properties");
            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            String locationsString = properties.getProperty("locations");
            List<String> locations = new ArrayList<>();
            if (locationsString != null) {
                String[] items = locationsString.split(",");
                locations.addAll(Arrays.asList(items));
            }

            return locations;
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Unit getUnits() {

        HourlyUnits hourlyUnits = getHourlyUnits();
        DailyUnits dailyUnits = getDailyUnits();

        return new Unit(hourlyUnits, dailyUnits);
    }

    DailyUnits getDailyUnits() {

        DailyUnits dailyUnits = new DailyUnits();
        List<DailyUnitsEntity> dailyUnitsEntities = dailyUnitsRepository.findAll();

        if (!dailyUnitsEntities.isEmpty()) {
            for (DailyUnitsEntity dailyUnitsEntity : dailyUnitsEntities) {
                if (dailyUnitsEntity.getParameter().equalsIgnoreCase("temperature_min"))
                    dailyUnits.setTemperatureMin(dailyUnitsEntity.getValue());
                if (dailyUnitsEntity.getParameter().equalsIgnoreCase("temperature_max"))
                    dailyUnits.setTemperatureMax(dailyUnitsEntity.getValue());
            }
        }

        return dailyUnits;
    }

    HourlyUnits getHourlyUnits() {

        HourlyUnits hourlyUnits = new HourlyUnits();
        List<HourlyUnitsEntity> hourlyUnitsEntities = hourlyUnitsRepository.findAll();

        if (!hourlyUnitsEntities.isEmpty()) {
            for (HourlyUnitsEntity hourlyUnitsEntity : hourlyUnitsEntities) {
                if (hourlyUnitsEntity.getParameter().equalsIgnoreCase("windspeed"))
                    hourlyUnits.setWindspeed(hourlyUnitsEntity.getValue());
                if (hourlyUnitsEntity.getParameter().equalsIgnoreCase("visibility"))
                    hourlyUnits.setVisibility(hourlyUnitsEntity.getValue());
                if (hourlyUnitsEntity.getParameter().equalsIgnoreCase("snowfall"))
                    hourlyUnits.setSnowfall(hourlyUnitsEntity.getValue());
                if (hourlyUnitsEntity.getParameter().equalsIgnoreCase("rain"))
                    hourlyUnits.setRain(hourlyUnitsEntity.getValue());
                if (hourlyUnitsEntity.getParameter().equalsIgnoreCase("temperature"))
                    hourlyUnits.setTemperature(hourlyUnitsEntity.getValue());
            }
        }

        return hourlyUnits;
    }

    void insertWeatherToDB(Weather weather) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            clearLocationData();
            insertHourlyUnits(weather);
            insertHourlyWeather(weather, formatter);
            insertDailyUnits(weather);
            insertDailyWeather(weather, formatter);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    void insertLocationInfo(String location, Weather weather) {
        try {
            LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
            locationInfoEntity.setLocation(location);
            locationInfoEntity.setLatitude(weather.latitude);
            locationInfoEntity.setLongitude(weather.longitude);
            locationInfoEntity.setElevation(weather.elevation);
            locationInfoEntity.setTimezone(weather.timezone);
            locationInfoEntity.setTimezoneAbbreviation(weather.timezoneAbbreviation);
            locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now()));
            locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now()));
            locationInfoRepository.save(locationInfoEntity);
            log.info("Location info inserted");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    void insertHourlyUnits(Weather weather) {
        try {
            HourlyUnitsEntity hourlyUnitsEntity1 = new HourlyUnitsEntity("time", weather.hourlyUnits.time);
            HourlyUnitsEntity hourlyUnitsEntity2 = new HourlyUnitsEntity("temperature", weather.hourlyUnits.temperature);
            HourlyUnitsEntity hourlyUnitsEntity3 = new HourlyUnitsEntity("rain", weather.hourlyUnits.rain);
            HourlyUnitsEntity hourlyUnitsEntity4 = new HourlyUnitsEntity("snowfall", weather.hourlyUnits.snowfall);
            HourlyUnitsEntity hourlyUnitsEntity5 = new HourlyUnitsEntity("visibility", weather.hourlyUnits.visibility);
            HourlyUnitsEntity hourlyUnitsEntity6 = new HourlyUnitsEntity("windspeed", weather.hourlyUnits.windspeed);
            List<HourlyUnitsEntity> hourlyUnitsEntities = Arrays.asList(hourlyUnitsEntity1, hourlyUnitsEntity2, hourlyUnitsEntity3, hourlyUnitsEntity4, hourlyUnitsEntity5, hourlyUnitsEntity6);
            hourlyUnitsRepository.saveAll(hourlyUnitsEntities);
            log.info("Hourly units added");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    void insertHourlyWeather(Weather weather, DateTimeFormatter formatter) {
        try {
            int hourlySize = weather.hourly.time.size();
            log.info(hourlySize);
            List<HourlyWeatherEntity> hourlyWeatherEntities = new ArrayList<>();
            for (int i = 0; i < hourlySize; i++) {
                HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
                hourlyWeatherEntity.setHourlyWeatherId(i + 1);
                LocalDateTime dateTime = LocalDateTime.parse(weather.hourly.time.get(i), formatter);
                hourlyWeatherEntity.setTime(Timestamp.valueOf(dateTime));
                hourlyWeatherEntity.setTemperature(weather.hourly.temperature.get(i));
                hourlyWeatherEntity.setRain(weather.hourly.rain.get(i));
                hourlyWeatherEntity.setSnowfall(weather.hourly.snowfall.get(i));
                hourlyWeatherEntity.setVisibility(weather.hourly.visibility.get(i));
                hourlyWeatherEntity.setWindSpeed(weather.hourly.windspeed.get(i));
                hourlyWeatherEntity.setUvIndex(weather.hourly.uvIndex.get(i));
                hourlyWeatherEntities.add(hourlyWeatherEntity);
            }
            hourlyWeatherRepository.saveAll(hourlyWeatherEntities);
            log.info("Hourly weather added");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    void insertDailyUnits(Weather weather) {
        try {
            DailyUnitsEntity dailyUnitsEntity1 = new DailyUnitsEntity("time", weather.dailyUnits.time);
            DailyUnitsEntity dailyUnitsEntity2 = new DailyUnitsEntity("temperature_max", weather.dailyUnits.temperatureMax);
            DailyUnitsEntity dailyUnitsEntity3 = new DailyUnitsEntity("temperature_min", weather.dailyUnits.temperatureMin);
            DailyUnitsEntity dailyUnitsEntity4 = new DailyUnitsEntity("sunrise", weather.dailyUnits.sunrise);
            DailyUnitsEntity dailyUnitsEntity5 = new DailyUnitsEntity("sunset", weather.dailyUnits.sunset);
            List<DailyUnitsEntity> dailyUnitsEntities = Arrays.asList(dailyUnitsEntity1, dailyUnitsEntity2, dailyUnitsEntity3, dailyUnitsEntity4, dailyUnitsEntity5);
            dailyUnitsRepository.saveAll(dailyUnitsEntities);
            log.info("Daily units added");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    void insertDailyWeather(Weather weather, DateTimeFormatter formatter) {
        try {
            int dailySize = weather.daily.time.size();
            log.info(dailySize);
            List<DailyWeatherEntity> dailyWeatherEntities = new ArrayList<>();
            for (int i = 0; i < dailySize; i++) {
                DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
                dailyWeatherEntity.setDailyWeatherId(i + 1);
                dailyWeatherEntity.setTime(Date.valueOf(weather.daily.time.get(i)));
                dailyWeatherEntity.setTemperatureMax(weather.daily.temperatureMax.get(i));
                dailyWeatherEntity.setTemperatureMin(weather.daily.temperatureMin.get(i));
                LocalDateTime sunriseDateTime = LocalDateTime.parse(weather.daily.sunrise.get(i), formatter);
                dailyWeatherEntity.setSunrise(Timestamp.valueOf(sunriseDateTime));
                LocalDateTime sunsetDateTime = LocalDateTime.parse(weather.daily.sunset.get(i), formatter);
                dailyWeatherEntity.setSunset(Timestamp.valueOf(sunsetDateTime));
                dailyWeatherEntity.setUvIndexMax(weather.daily.uvIndexMax.get(i));
                dailyWeatherEntities.add(dailyWeatherEntity);
            }
            dailyWeatherRepository.saveAll(dailyWeatherEntities);
            log.info("Daily weather added");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public List<String> validatedLocations() {
        List<String> validatedLocations = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource("locations.properties");
            Properties properties = new Properties();
            properties.load(resource.getInputStream());

            String locationsString = properties.getProperty("locations");
            List<String> locations = new ArrayList<>();
            if (locationsString != null) {
                String[] items = locationsString.split(",");
                locations.addAll(Arrays.asList(items));
            }

            for (String location : locations) {
                Map<String, String> locationDetail = externalAPIUtil.getLocationDetails(location);
                if (!locationDetail.isEmpty())
                    validatedLocations.add(location);
            }

            validatedLocations.stream().distinct().forEach(System.out::println);

            return validatedLocations;
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

}
