package com.erkan.weather_api.service;

import com.erkan.weather_api.dto.WeatherDto;
import com.erkan.weather_api.httpclient.WeatherApiClient;
import com.erkan.weather_api.httpclient.response.Forecast;
import com.erkan.weather_api.httpclient.response.WeatherApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WeatherInfoService {
    private static final int EVENT_DURATION_IN_HOURS = 2;
    private final WeatherApiClient weatherApiClient;

    @Autowired
    public WeatherInfoService(WeatherApiClient weatherApiClient) {
        this.weatherApiClient = weatherApiClient;
    }

    public List<WeatherDto> getWeatherInfo(float latitude, float longitude, String eventTime) {
        ZonedDateTime eventDateTime = ZonedDateTime.parse(eventTime);
        ZonedDateTime startTime = roundMinutesToZero(eventDateTime);
        ZonedDateTime endTime = startTime.plusHours(EVENT_DURATION_IN_HOURS);

        List<Forecast> forecasts = fetchForecasts(latitude, longitude, startTime, endTime);
        return WeatherDto.mapToWeatherDtoList(forecasts);
    }

    private ZonedDateTime roundMinutesToZero(ZonedDateTime dateTime) {
        return (dateTime.getMinute() != 0) ? dateTime.withMinute(0) : dateTime;
    }

    private List<Forecast> fetchForecasts(float latitude, float longitude, ZonedDateTime startTime, ZonedDateTime endTime) {
        ResponseEntity<WeatherApiResponse> responseEntity = weatherApiClient.getWeatherInfo(latitude, longitude);
        return responseEntity.getBody()
                .getProperties()
                .getTimeseries()
                .stream()
                .filter(forecast -> isWithinTimeRange(forecast.getTime(), startTime, endTime))
                .collect(Collectors.toList());
    }

    private boolean isWithinTimeRange(ZonedDateTime forecastTime, ZonedDateTime startTime, ZonedDateTime endTime) {
        return (forecastTime.isEqual(startTime) || forecastTime.isEqual(endTime)) ||
                (forecastTime.isAfter(startTime) && forecastTime.isBefore(endTime));
    }
}
