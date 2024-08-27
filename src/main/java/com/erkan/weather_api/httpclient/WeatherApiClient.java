package com.erkan.weather_api.httpclient;

import com.erkan.weather_api.exception.ExternalApiException;
import com.erkan.weather_api.httpclient.response.WeatherApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WeatherApiClient {
    private final RestTemplate restTemplate;
    @Value("${weather-api-url}")
    private String weatherApiUrl = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%f&lon=%f";
    private ConcurrentHashMap<String, ZonedDateTime> lastModifiedStore = new ConcurrentHashMap<>();

    @Autowired
    public WeatherApiClient(RestTemplateBuilder restTemplateBuilder) {
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            request.getHeaders().add("user-agent", "weather-api/0.1 https://github.com/erkantomruk/weather-api");
            return execution.execute(request, body);
        };
        restTemplate = restTemplateBuilder.additionalInterceptors(interceptor).build();
    }

    @Cacheable("WeatherApiCache")
    public ResponseEntity<WeatherApiResponse> getWeatherInfo(float latitude, float longitude) {
        String url = buildUrl(latitude, longitude);
        HttpHeaders headers = createHttpHeaders(url);

        try {
            ResponseEntity<WeatherApiResponse> response = restTemplate.getForEntity(url, WeatherApiResponse.class, headers);
            handleLastModifiedHeader(url, response);
            return buildResponseEntity(response);
        } catch (HttpStatusCodeException e) {
            return handleHttpStatusCodeException(e);
        }
    }

    private String buildUrl(float latitude, float longitude) {
        return String.format(weatherApiUrl, latitude, longitude);
    }

    private HttpHeaders createHttpHeaders(String url) {
        HttpHeaders headers = new HttpHeaders();
        if (lastModifiedStore.containsKey(url)) {
            headers.setIfModifiedSince(lastModifiedStore.get(url));
        }
        return headers;
    }

    private void handleLastModifiedHeader(String url, ResponseEntity<WeatherApiResponse> response) {
        if (response.getStatusCode() == HttpStatus.OK && response.getHeaders().containsKey(HttpHeaders.LAST_MODIFIED)) {
            ZonedDateTime lastModified = ZonedDateTime.parse(response.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED),
                    DateTimeFormatter.RFC_1123_DATE_TIME);

            lastModifiedStore.put(url, lastModified);
        }
    }

    private ResponseEntity<WeatherApiResponse> buildResponseEntity(ResponseEntity<WeatherApiResponse> response) {
        return response.getStatusCode() == HttpStatus.OK ? response :
                ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    private ResponseEntity<WeatherApiResponse> handleHttpStatusCodeException(HttpStatusCodeException e) {
        if (e.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        throw new ExternalApiException("Failed to call external API", e);
    }
}
