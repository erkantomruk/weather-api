package com.erkan.weather_api.dto;

import com.erkan.weather_api.httpclient.response.Forecast;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WeatherDto {
    private ZonedDateTime time;
    private float airTemperature;
    private float windSpeed;

    public static List<WeatherDto> mapToWeatherDtoList(List<Forecast> forecasts) {
        return forecasts.stream()
                .map(forecast -> new WeatherDto(
                        forecast.getTime(),
                        forecast.getData().getInstant().getDetails().getAirTemperature(),
                        forecast.getData().getInstant().getDetails().getWindSpeed()))
                .collect(Collectors.toList());
    }
}
