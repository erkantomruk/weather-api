package com.erkan.weather_api.httpclient;

import com.erkan.weather_api.exception.ExternalApiException;
import com.erkan.weather_api.httpclient.response.WeatherApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WeatherApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private WeatherApiClient weatherApiClient;

    private ConcurrentHashMap<String, ZonedDateTime> lastModifiedStore;

    @Value("${weather-api-url}")
    private String weatherApiUrl = "https://api.weather.com/v3/weather?lat=%f&lon=%f";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(restTemplateBuilder.additionalInterceptors(any(ClientHttpRequestInterceptor.class)))
                .thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        weatherApiClient = new WeatherApiClient(restTemplateBuilder);

        lastModifiedStore = new ConcurrentHashMap<>();
    }

    @Test
    void getWeatherInfoCacheHitAndModified() {
        float latitude = 10.10f;
        float longitude = 20.20f;
        String url = String.format(weatherApiUrl, latitude, longitude);

        ZonedDateTime lastModifiedTime = ZonedDateTime.now().minusDays(1);
        lastModifiedStore.put(url, lastModifiedTime);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LAST_MODIFIED, lastModifiedTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));

        WeatherApiResponse mockResponse = new WeatherApiResponse();
        ResponseEntity<WeatherApiResponse> responseEntity = new ResponseEntity<>(mockResponse, headers, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class)))
                .thenReturn(responseEntity);

        ResponseEntity<WeatherApiResponse> response = weatherApiClient.getWeatherInfo(latitude, longitude);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class));
    }

    @Test
    void getWeatherInfoReturnsNotModifiedResponse() {
        float latitude = 10.10f;
        float longitude = 20.20f;
        String url = String.format(weatherApiUrl, latitude, longitude);

        ZonedDateTime lastModifiedTime = ZonedDateTime.now().minusDays(1);
        lastModifiedStore.put(url, lastModifiedTime);

        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_MODIFIED);

        when(restTemplate.getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class)))
                .thenThrow(exception);

        ResponseEntity<WeatherApiResponse> response = weatherApiClient.getWeatherInfo(latitude, longitude);

        assertEquals(HttpStatus.NOT_MODIFIED, response.getStatusCode());
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class));
    }

    @Test
    void getWeatherInfoReturnsHttpErrorResponse() {
        float latitude = 10.10f;
        float longitude = 20.20f;

        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        when(restTemplate.getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class)))
                .thenThrow(exception);

        ExternalApiException thrown = assertThrows(ExternalApiException.class, () ->
                weatherApiClient.getWeatherInfo(latitude, longitude));

        assertEquals("Failed to call external API", thrown.getMessage());
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class));
    }

    @Test
    void getWeatherInfoDoesNotHitCache() {
        float latitude = 10.10f;
        float longitude = 20.20f;

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LAST_MODIFIED, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

        WeatherApiResponse mockResponse = new WeatherApiResponse();
        ResponseEntity<WeatherApiResponse> responseEntity = new ResponseEntity<>(mockResponse, headers, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class)))
                .thenReturn(responseEntity);

        ResponseEntity<WeatherApiResponse> response = weatherApiClient.getWeatherInfo(latitude, longitude);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(WeatherApiResponse.class), any(HttpHeaders.class));
    }
}