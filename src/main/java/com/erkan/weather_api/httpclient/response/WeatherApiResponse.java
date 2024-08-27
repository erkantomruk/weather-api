package com.erkan.weather_api.httpclient.response;

import lombok.*;

@Builder
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeatherApiResponse {
    private WeatherProperty properties;
}
