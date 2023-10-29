package com.hacker.meteomaster.service.impl;

import com.hacker.meteomaster.bo.*;
import com.hacker.meteomaster.dto.CurrentWeather;
import com.hacker.meteomaster.dto.Day;
import com.hacker.meteomaster.dto.Location;
import com.hacker.meteomaster.dto.Week;
import com.hacker.meteomaster.entity.DailyWeatherEntity;
import com.hacker.meteomaster.entity.HourlyWeatherEntity;
import com.hacker.meteomaster.entity.LocationInfoEntity;
import com.hacker.meteomaster.repository.*;
import com.hacker.meteomaster.util.ExternalAPIUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.hacker.meteomaster.constant.DataConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MeteoServiceImplTest {

    @Mock
    private DailyWeatherRepository dailyWeatherRepository;
    @Mock
    private DailyUnitsRepository dailyUnitsRepository;
    @Mock
    private HourlyWeatherRepository hourlyWeatherRepository;
    @Mock
    private HourlyUnitsRepository hourlyUnitsRepository;
    @Mock
    private LocationInfoRepository locationInfoRepository;
    @Mock
    private ExternalAPIUtil externalAPIUtil;

    @InjectMocks
    private MeteoServiceImpl underTest;

    @Test
    void testSetLocationWithEmptyLocationInfoEntities() {
        when(locationInfoRepository.findAll()).thenReturn(Collections.emptyList());
        ResponseEntity<String> response = underTest.setLocation("NewLocation");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testSetLocationWithLastUpdatedBeforeToday() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        Map<String, String> locationMap = new HashMap<>();
        locationMap.put(LOCATION, city);
        locationMap.put(LATITUDE, String.valueOf(latitude));
        locationMap.put(LONGITUDE, String.valueOf(longitude));

        Hourly hourly = new Hourly();
        HourlyUnits hourlyUnits = new HourlyUnits();
        Daily daily = new Daily();
        DailyUnits dailyUnits = new DailyUnits();
        hourly.time = Collections.emptyList();
        daily.time = Collections.emptyList();

        Weather weatherData = new Weather();
        weatherData.latitude = latitude;
        weatherData.longitude = longitude;
        weatherData.elevation = elevation;
        weatherData.hourly = hourly;
        weatherData.hourlyUnits = hourlyUnits;
        weatherData.daily = daily;
        weatherData.dailyUnits = dailyUnits;

        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(externalAPIUtil.getLocationDetails(any())).thenReturn(locationMap);
        when(externalAPIUtil.getWeatherData(any(), any())).thenReturn(weatherData);

        ResponseEntity<String> response = underTest.setLocation(city);

        verify(locationInfoRepository, times(1)).save(any(LocationInfoEntity.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(LOCATION_SET_SUCCESS, response.getBody());
    }

    @Test
    void testSetLocationWithLastUpdatedToday() {
        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now()));
        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));

        ResponseEntity<String> response = underTest.setLocation("NewLocation");

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(LOCATION_UPDATE_NOT_ALLOWED, response.getBody());
    }

    @Test
    void testSetLocationWithEmptyLocationDetail() {
        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));

        when(externalAPIUtil.getLocationDetails(any())).thenReturn(Collections.emptyMap());

        ResponseEntity<String> response = underTest.setLocation("NewLocation");

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(LOCATION_FETCH_ERROR, response.getBody());
    }

    @Test
    void testSetLocationWithInternalError() {
        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        Map<String, String> locationMap = new HashMap<>();
        locationMap.put(LOCATION, city);
        locationMap.put(LATITUDE, String.valueOf(latitude));
        locationMap.put(LONGITUDE, String.valueOf(longitude));

        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));

        when(externalAPIUtil.getLocationDetails(any())).thenReturn(locationMap);
        when(externalAPIUtil.getWeatherData(any(), any())).thenReturn(null);

        ResponseEntity<String> response = underTest.setLocation("NewLocation");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(LOCATION_UPDATE_ERROR, response.getBody());
    }

    @Test
    void testGetLocationWithEmptyLocationInfoEntities() {
        when(locationInfoRepository.findAll()).thenReturn(Collections.emptyList());
        Location result = underTest.getLocation();
        assertNull(result);
    }

    @Test
    void testGetLocationWithNonEmptyLocationInfoEntities() {

        String location = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;
        String timezone = "Eastern Standard Time (EST)";
        String timezoneAbbr = "EST";

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(location);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone(timezone);
        locationInfoEntity.setTimezoneAbbreviation(timezoneAbbr);

        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));

        Location result = underTest.getLocation();

        assertNotNull(result);
        assertEquals(location, result.getLocationName());
        assertEquals(latitude, result.getLatitude());
        assertEquals(longitude, result.getLongitude());
        assertEquals(elevation, result.getElevation());
        assertEquals(timezone, result.getTimezone());
        assertEquals(timezoneAbbr, result.getTimezoneAbbreviation());

        assertTrue(result.getCurrentTime().isBefore(LocalDateTime.now().plusMinutes(1)));
        assertTrue(result.getCurrentTime().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void testGetCurrentWeatherWithEmptyLocationInfoEntities() {
        when(locationInfoRepository.findAll()).thenReturn(Collections.emptyList());
        CurrentWeather result = underTest.getCurrentWeather();
        assertNull(result);
    }

    @Test
    void testGetCurrentWeatherWithOptionalDataPresent() {
        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(25.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(5.0);
        hourlyWeatherEntity.setWindSpeed(10.0);
        hourlyWeatherEntity.setVisibility(5.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        CurrentWeather result = underTest.getCurrentWeather();

        assertNotNull(result);
    }

    @Test
    void testGetCurrentWeatherWithNoOptionalDataPresent() {
        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(Optional.empty());
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(Optional.empty());

        CurrentWeather result = underTest.getCurrentWeather();

        assertNotNull(result);
    }

    @Test
    void testGetDailyWeatherWithEmptyLocationInfoEntities() {
        when(locationInfoRepository.findAll()).thenReturn(Collections.emptyList());
        List<Day> result = underTest.getDailyWeather();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetDailyWeatherWithValidData() {
        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));
        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));

        List<HourlyWeatherEntity> hourlyWeatherEntities = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
            hourlyWeatherEntity.setTemperature(25.0 + i);
            hourlyWeatherEntity.setRain(0.0);
            hourlyWeatherEntity.setSnowfall(0.0);
            hourlyWeatherEntity.setUvIndex(5.0 + i);
            hourlyWeatherEntity.setWindSpeed(10.0 + i);
            hourlyWeatherEntity.setVisibility(5.0 + i);
            hourlyWeatherEntities.add(hourlyWeatherEntity);
        }

        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(Optional.of(hourlyWeatherEntities.get(0)), Optional.of(hourlyWeatherEntities.get(1)), Optional.of(hourlyWeatherEntities.get(2)), Optional.of(hourlyWeatherEntities.get(3)), Optional.of(hourlyWeatherEntities.get(4)));

        List<Day> result = underTest.getDailyWeather();

        assertEquals(5, result.size());
    }

    @Test
    void testGetWeeklyWeatherWithEmptyLocationInfoEntities() {
        when(locationInfoRepository.findAll()).thenReturn(Collections.emptyList());
        List<Week> result = underTest.getWeeklyWeather();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetWeeklyWeatherWithValidData() {
        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));
        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));

        List<DailyWeatherEntity> dailyWeatherEntities = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().plusDays(i);

            DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
            dailyWeatherEntity.setTemperatureMax(30.0 + i);
            dailyWeatherEntity.setTemperatureMin(20.0 + i);
            dailyWeatherEntity.setSunrise(Timestamp.valueOf(localDate.atTime(6, 0, 0, 0)));
            dailyWeatherEntity.setSunset(Timestamp.valueOf(localDate.atTime(18, 0, 0, 0)));
            dailyWeatherEntity.setUvIndexMax(8.0 + i);

            dailyWeatherEntities.add(dailyWeatherEntity);
        }

        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(Optional.of(dailyWeatherEntities.get(0)), Optional.of(dailyWeatherEntities.get(1)), Optional.of(dailyWeatherEntities.get(2)), Optional.of(dailyWeatherEntities.get(3)), Optional.of(dailyWeatherEntities.get(4)), Optional.of(dailyWeatherEntities.get(5)), Optional.of(dailyWeatherEntities.get(6)));

        List<Week> result = underTest.getWeeklyWeather();

        assertEquals(7, result.size());
    }

    @Test
    void testDetermineCurrentWeatherConditionCloudyWindy() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(25.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(5.0);
        hourlyWeatherEntity.setWindSpeed(25.0);
        hourlyWeatherEntity.setVisibility(5.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String condition = underTest.determineCurrentWeatherCondition();
        assertEquals(CLOUDY_WINDY, condition);
    }

    @Test
    void testDetermineCurrentWeatherConditionCloudy() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(25.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(5.0);
        hourlyWeatherEntity.setWindSpeed(15.0);
        hourlyWeatherEntity.setVisibility(5.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String condition = underTest.determineCurrentWeatherCondition();
        assertEquals(CLOUDY, condition);
    }

    @Test
    void testDetermineCurrentWeatherConditionSunny() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(15.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(5.0);
        hourlyWeatherEntity.setWindSpeed(15.0);
        hourlyWeatherEntity.setVisibility(5.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String condition = underTest.determineCurrentWeatherCondition();
        assertEquals(SUNNY, condition);
    }

    @Test
    void testDetermineCurrentWeatherConditionFog() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(10.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(5.0);
        hourlyWeatherEntity.setWindSpeed(15.0);
        hourlyWeatherEntity.setVisibility(50.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String condition = underTest.determineCurrentWeatherCondition();
        assertEquals(FOG, condition);
    }

    @Test
    void testDetermineCurrentWeatherConditionHail() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(5.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(8.0);
        hourlyWeatherEntity.setWindSpeed(15.0);
        hourlyWeatherEntity.setVisibility(125.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String condition = underTest.determineCurrentWeatherCondition();
        assertEquals(HAIL, condition);
    }

    @Test
    void testGetAlertsBlizzardAlert() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(5.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(16.0);
        hourlyWeatherEntity.setUvIndex(8.0);
        hourlyWeatherEntity.setWindSpeed(15.0);
        hourlyWeatherEntity.setVisibility(150.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String alert = underTest.getAlerts();
        assertEquals(BLIZZARD_ALERT, alert);
    }

    @Test
    void testGetAlertsThunderstormAlert() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(5.0);
        hourlyWeatherEntity.setRain(55.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(8.0);
        hourlyWeatherEntity.setWindSpeed(75.0);
        hourlyWeatherEntity.setVisibility(125.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String alert = underTest.getAlerts();
        assertEquals(THUNDERSTORM_ALERT, alert);
    }

    @Test
    void testGetAlertsHighSunburnAlert() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(5.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(8.0);
        hourlyWeatherEntity.setWindSpeed(15.0);
        hourlyWeatherEntity.setVisibility(125.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String alert = underTest.getAlerts();
        assertEquals(HIGH_SUNBURN_ALERT, alert);
    }

    @Test
    void testGetAlertsNoAlert() {

        String city = "New York City";
        double latitude = 40.7128;
        double longitude = -74.0060;
        double elevation = 10.5;

        LocationInfoEntity locationInfoEntity = new LocationInfoEntity();
        locationInfoEntity.setLocation(city);
        locationInfoEntity.setLatitude(latitude);
        locationInfoEntity.setLongitude(longitude);
        locationInfoEntity.setElevation(elevation);
        locationInfoEntity.setTimezone("Eastern Standard Time (EST)");
        locationInfoEntity.setTimezoneAbbreviation("EST");
        locationInfoEntity.setLastLocationChange(Date.valueOf(LocalDate.now().minusDays(1)));
        locationInfoEntity.setLastDataRefresh(Date.valueOf(LocalDate.now().minusDays(1)));

        HourlyWeatherEntity hourlyWeatherEntity = new HourlyWeatherEntity();
        hourlyWeatherEntity.setTemperature(5.0);
        hourlyWeatherEntity.setRain(0.0);
        hourlyWeatherEntity.setSnowfall(0.0);
        hourlyWeatherEntity.setUvIndex(6.0);
        hourlyWeatherEntity.setWindSpeed(15.0);
        hourlyWeatherEntity.setVisibility(125.0);
        Optional<HourlyWeatherEntity> optionalHourlyWeatherEntity = Optional.of(hourlyWeatherEntity);

        DailyWeatherEntity dailyWeatherEntity = new DailyWeatherEntity();
        dailyWeatherEntity.setTemperatureMax(30.0);
        dailyWeatherEntity.setTemperatureMin(20.0);
        dailyWeatherEntity.setSunrise(Timestamp.valueOf(LocalDateTime.now().withHour(6).withMinute(0).withSecond(0)));
        dailyWeatherEntity.setSunset(Timestamp.valueOf(LocalDateTime.now().withHour(18).withMinute(0).withSecond(0)));
        Optional<DailyWeatherEntity> optionalDailyWeatherEntity = Optional.of(dailyWeatherEntity);


        when(locationInfoRepository.findAll()).thenReturn(Collections.singletonList(locationInfoEntity));
        when(hourlyWeatherRepository.findByTime(any(Timestamp.class))).thenReturn(optionalHourlyWeatherEntity);
        when(dailyWeatherRepository.findByTime(any(Date.class))).thenReturn(optionalDailyWeatherEntity);

        String alert = underTest.getAlerts();
        assertNull(alert);
    }

}