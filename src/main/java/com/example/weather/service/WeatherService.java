package com.example.weather.service;

import com.example.weather.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private final WebClient weatherWebClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    public WeatherService(WebClient weatherWebClient) {
        this.weatherWebClient = weatherWebClient;
    }

    @Cacheable(value = "currentWeather", key = "#city.toLowerCase()")
    public WeatherResponse getCurrentWeather(String city) {
        requireApiKey();
        try {
            Map<String, Object> json = weatherWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("q", city)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403, response ->
                            Mono.error(new IllegalStateException("OpenWeatherMap API key is invalid or not authorized (HTTP " + response.statusCode().value() + ").")))
                    .onStatus(status -> status.value() == 429, response ->
                            Mono.error(new IllegalStateException("OpenWeatherMap rate limit exceeded (HTTP 429). Try again later.")))
                    .onStatus(status -> status.value() == 404, response ->
                            Mono.error(new IllegalArgumentException("City not found: " + city)))
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            Mono.error(new IllegalArgumentException("Request failed (HTTP " + response.statusCode().value() + "). Check city name and try again.")))
                    .bodyToMono(Map.class)
                    .block();

            if (json == null) {
                throw new IllegalStateException("Empty response from weather service");
            }

            return mapCurrentWeather(json);
        } catch (WebClientResponseException.BadRequest e) {
            throw new IllegalArgumentException("Invalid city name: " + city);
        }
    }

    @Cacheable(value = "forecast", key = "#city.toLowerCase()")
    public WeatherResponse getFiveDayForecast(String city) {
        requireApiKey();
        try {
            Map<String, Object> json = weatherWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("q", city)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403, response ->
                            Mono.error(new IllegalStateException("OpenWeatherMap API key is invalid or not authorized (HTTP " + response.statusCode().value() + ").")))
                    .onStatus(status -> status.value() == 429, response ->
                            Mono.error(new IllegalStateException("OpenWeatherMap rate limit exceeded (HTTP 429). Try again later.")))
                    .onStatus(status -> status.value() == 404, response ->
                            Mono.error(new IllegalArgumentException("City not found: " + city)))
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            Mono.error(new IllegalArgumentException("Request failed (HTTP " + response.statusCode().value() + "). Check city name and try again.")))
                    .bodyToMono(Map.class)
                    .block();

            if (json == null) {
                throw new IllegalStateException("Empty response from weather service");
            }

            WeatherResponse response = new WeatherResponse();

            Map<String, Object> cityInfo = (Map<String, Object>) json.get("city");
            if (cityInfo != null) {
                response.setCity((String) cityInfo.get("name"));
                Map<String, Object> countryObj = cityInfo;
                Object country = countryObj.get("country");
                if (country != null) {
                    response.setCountry(country.toString());
                }
            }

            List<Map<String, Object>> list = (List<Map<String, Object>>) json.get("list");
            List<WeatherResponse.ForecastEntry> forecastEntries = new ArrayList<>();

            if (list != null) {
                for (Map<String, Object> entry : list) {
                    WeatherResponse.ForecastEntry f = new WeatherResponse.ForecastEntry();

                    Object dtObj = entry.get("dt");
                    if (dtObj != null) {
                        long epochSeconds = Long.parseLong(dtObj.toString());
                        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds),
                                ZoneId.systemDefault());
                        f.setDateTime(dateTime);
                    }

                    Map<String, Object> main = (Map<String, Object>) entry.get("main");
                    if (main != null) {
                        Object tempObj = main.get("temp");
                        if (tempObj != null) {
                            double tempC = Double.parseDouble(tempObj.toString());
                            f.setTemperatureCelsius(tempC);
                        }
                        Object humidityObj = main.get("humidity");
                        if (humidityObj != null) {
                            f.setHumidity(Integer.parseInt(humidityObj.toString()));
                        }
                    }

                    List<Map<String, Object>> weatherArr =
                            (List<Map<String, Object>>) entry.get("weather");
                    if (weatherArr != null && !weatherArr.isEmpty()) {
                        Map<String, Object> w = weatherArr.get(0);
                        Object desc = w.get("description");
                        if (desc != null) {
                            f.setCondition(desc.toString());
                        }
                        Object icon = w.get("icon");
                        if (icon != null) {
                            f.setIcon(icon.toString());
                        }
                    }

                    forecastEntries.add(f);
                }
            }

            response.setForecast(forecastEntries);
            return response;
        } catch (WebClientResponseException.BadRequest e) {
            throw new IllegalArgumentException("Invalid city name: " + city);
        }
    }

    private void requireApiKey() {
        if (apiKey == null) {
            throw new IllegalStateException("Missing OpenWeatherMap API key. Set openweather.api.key in application.properties.");
        }
        String trimmed = apiKey.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("YOUR_")) {
            throw new IllegalStateException("Missing OpenWeatherMap API key. Set openweather.api.key in application.properties.");
        }
    }

    private WeatherResponse mapCurrentWeather(Map<String, Object> json) {
        WeatherResponse response = new WeatherResponse();

        Map<String, Object> main = (Map<String, Object>) json.get("main");
        if (main != null) {
            Object tempObj = main.get("temp");
            if (tempObj != null) {
                double tempC = Double.parseDouble(tempObj.toString());
                response.setTemperatureCelsius(tempC);
            }
            Object humidityObj = main.get("humidity");
            if (humidityObj != null) {
                response.setHumidity(Integer.parseInt(humidityObj.toString()));
            }
        }

        List<LinkedHashMap<String, Object>> weatherArr =
                (List<LinkedHashMap<String, Object>>) json.get("weather");
        if (weatherArr != null && !weatherArr.isEmpty()) {
            Map<String, Object> w = weatherArr.get(0);
            Object desc = w.get("description");
            if (desc != null) {
                response.setCondition(desc.toString());
            }
            Object icon = w.get("icon");
            if (icon != null) {
                response.setIcon(icon.toString());
            }
        }

        Map<String, Object> sys = (Map<String, Object>) json.get("sys");
        if (sys != null) {
            Object country = sys.get("country");
            if (country != null) {
                response.setCountry(country.toString());
            }
        }

        Object name = json.get("name");
        if (name != null) {
            response.setCity(name.toString());
        }

        Object dtObj = json.get("dt");
        if (dtObj != null) {
            long epochSeconds = Long.parseLong(dtObj.toString());
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(epochSeconds),
                    ZoneId.systemDefault()
            );
            response.setTimestamp(dateTime);
        }

        return response;
    }
}

