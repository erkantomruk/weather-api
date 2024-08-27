package com.erkan.weather_api.httpclient.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WeatherDetails {
    @JsonProperty("air_temperature")
    private float airTemperature;
    @JsonProperty("wind_speed")
    private float windSpeed;
}
