package com.erkan.weather_api.controller;

import com.erkan.weather_api.dto.WeatherDto;
import com.erkan.weather_api.service.WeatherInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/weather")
public class WeatherInfoController {
    private final WeatherInfoService weatherInfoService;

    @Autowired
    public WeatherInfoController(WeatherInfoService weatherInfoService) {
        this.weatherInfoService = weatherInfoService;
    }

    @GetMapping
    @Validated
    public List<WeatherDto> getWeatherInfo(@RequestParam float latitude, @RequestParam float longitude,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String eventTime) {
        validateLatitudeAndLongitude(latitude, longitude);
        return weatherInfoService.getWeatherInfo(latitude, longitude, eventTime);
    }

    private void validateLatitudeAndLongitude(float latitude, float longitude) {
        if (!hasMaxFourDecimals(latitude) || !hasMaxFourDecimals(longitude)) {
            throw new IllegalArgumentException("Latitude and Longitude must have a maximum of 4 decimal places.");
        }
    }

    private boolean hasMaxFourDecimals(Float number) {
        String[] parts = String.valueOf(number).split("\\.");
        return parts.length == 1 || (parts.length == 2 && parts[1].length() <= 4);
    }
}
