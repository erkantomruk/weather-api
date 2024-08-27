package com.erkan.weather_api.controller;

import com.erkan.weather_api.dto.WeatherDto;
import com.erkan.weather_api.service.WeatherInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WeatherInfoController.class)
public class WeatherInfoControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherInfoService weatherInfoService;

    @Test
    public void getWeatherInfoReturnsForecast() throws Exception {
        float latitude = 10.10f;
        float longitude = 20.20f;
        String eventTime = "2024-08-26T22:00:00Z";

        WeatherDto forecast1 = WeatherDto.builder().airTemperature(13).windSpeed(15).time(ZonedDateTime.parse(eventTime)).build();

        WeatherDto forecast2 = WeatherDto.builder().airTemperature(20).windSpeed(2).time(ZonedDateTime.parse(eventTime)).build();
        List<WeatherDto> forecastList = Arrays.asList(forecast1, forecast2);

        when(weatherInfoService.getWeatherInfo(latitude, longitude, eventTime)).thenReturn(forecastList);

        mockMvc.perform(get("/weather?latitude=10.10&longitude=20.20&eventTime=2024-08-26T22:00:00Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].airTemperature").value(13.0))
                .andExpect(jsonPath("$[0].windSpeed").value(15.0))
                .andExpect(jsonPath("$[1]airTemperature").value(20.0))
                .andExpect(jsonPath("$[1].windSpeed").value(2.0));

        verify(weatherInfoService, times(1)).getWeatherInfo(latitude, longitude, eventTime);
    }
    @Test
    public void getWeatherInfoShouldReturnBadRequestForInvalidLatitude() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/weather")
                        .param("latitude", "10.12345")  // More than 4 decimal places
                        .param("longitude", "20.1234")
                        .param("eventTime", "2024-08-01T00:00:00Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getWeatherInfoShouldReturnBadRequestForInvalidLongitude() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/weather")
                        .param("latitude", "10.1234")
                        .param("longitude", "20.12345")  // More than 4 decimal places
                        .param("eventTime", "2024-08-01T00:00:00Z")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}