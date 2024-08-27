package com.erkan.weather_api.httpclient.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeatherProperty {
    private List<Forecast> timeseries;

}
