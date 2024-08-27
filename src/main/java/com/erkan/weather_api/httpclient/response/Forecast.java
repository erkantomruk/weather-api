package com.erkan.weather_api.httpclient.response;

import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Forecast {
    private ZonedDateTime time;
    private WeatherData data;
}
