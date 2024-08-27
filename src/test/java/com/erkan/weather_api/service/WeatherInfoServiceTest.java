package com.erkan.weather_api.service;

import com.erkan.weather_api.dto.WeatherDto;
import com.erkan.weather_api.httpclient.WeatherApiClient;
import com.erkan.weather_api.httpclient.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class WeatherInfoServiceTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @InjectMocks
    private WeatherInfoService weatherInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWeatherInfoReturnsForecast() {
        float latitude = 10.10f;
        float longitude = 20.20f;
        String eventTime = "2024-08-27T10:00:00Z";
        ZonedDateTime eventDateTime = ZonedDateTime.parse(eventTime);
        ZonedDateTime roundedTime = eventDateTime.withMinute(0);
        ZonedDateTime endTime = roundedTime.plusHours(2);

        Forecast forecast1 = createForecast(roundedTime);
        Forecast forecast2 = createForecast(roundedTime.plusHours(1));
        Forecast forecast3 = createForecast(endTime);

        WeatherApiResponse apiResponse = new WeatherApiResponse();
        apiResponse.setProperties(new WeatherProperty());
        apiResponse.getProperties().setTimeseries(Arrays.asList(forecast1, forecast2, forecast3));

        when(weatherApiClient.getWeatherInfo(latitude, longitude)).thenReturn(ResponseEntity.ok(apiResponse));

        List<WeatherDto> result = weatherInfoService.getWeatherInfo(latitude, longitude, eventTime);

        assertEquals(3, result.size());
        assertEquals(forecast1.getTime(), result.get(0).getTime());
        assertEquals(forecast1.getData().getInstant().getDetails().getAirTemperature(), result.get(0).getAirTemperature());
    }

    @Test
    void getWeatherInfoRoundsMinutes() {
        float latitude = 10.10f;
        float longitude = 20.20f;
        String eventTime = "2024-08-27T10:15:00Z";
        ZonedDateTime eventDateTime = ZonedDateTime.parse(eventTime);
        ZonedDateTime roundedTime = eventDateTime.withMinute(0);
        ZonedDateTime endTime = roundedTime.plusHours(2);

        Forecast forecast1 = createForecast(roundedTime);
        Forecast forecast2 = createForecast(roundedTime.plusHours(1));
        Forecast forecast3 = createForecast(endTime);

        WeatherApiResponse apiResponse = new WeatherApiResponse();
        apiResponse.setProperties(new WeatherProperty());
        apiResponse.getProperties().setTimeseries(Arrays.asList(forecast1, forecast2, forecast3));

        when(weatherApiClient.getWeatherInfo(latitude, longitude)).thenReturn(ResponseEntity.ok(apiResponse));

        List<WeatherDto> result = weatherInfoService.getWeatherInfo(latitude, longitude, eventTime);

        assertEquals(3, result.size());
        assertEquals(forecast1.getTime(), result.get(0).getTime());
        assertEquals(forecast1.getData().getInstant().getDetails().getAirTemperature(), result.get(0).getAirTemperature());
    }

    private Forecast createForecast(ZonedDateTime time) {
        Forecast forecast = new Forecast();
        forecast.setTime(time);
        WeatherData instant = WeatherData.builder().instant(InstantWeather.builder()
                .details(WeatherDetails.builder()
                        .windSpeed(5.0f).airTemperature(20.0f).build()).build()).build();
        forecast.setData(instant);
        return forecast;
    }
}