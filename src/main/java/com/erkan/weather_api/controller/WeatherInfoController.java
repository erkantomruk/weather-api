package com.erkan.weather_api.controller;

import com.erkan.weather_api.dto.WeatherDto;
import com.erkan.weather_api.service.WeatherInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
    public List<WeatherDto> getWeatherInfo(@RequestParam float latitude, @RequestParam float longitude,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String eventTime) {
        return weatherInfoService.getWeatherInfo(latitude, longitude, eventTime);
    }
}
