package com.example.weather.model;

import java.time.LocalDateTime;
import java.util.List;

public class WeatherResponse {

    private String city;
    private String country;
    private double temperatureCelsius;
    private double temperatureFahrenheit;
    private int humidity;
    private String condition;
    private String icon;
    private LocalDateTime timestamp;
    private List<ForecastEntry> forecast;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
        this.temperatureFahrenheit = temperatureCelsius * 9 / 5 + 32;
    }

    public double getTemperatureFahrenheit() {
        return temperatureFahrenheit;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<ForecastEntry> getForecast() {
        return forecast;
    }

    public void setForecast(List<ForecastEntry> forecast) {
        this.forecast = forecast;
    }

    public static class ForecastEntry {
        private LocalDateTime dateTime;
        private double temperatureCelsius;
        private double temperatureFahrenheit;
        private int humidity;
        private String condition;
        private String icon;

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }

        public double getTemperatureCelsius() {
            return temperatureCelsius;
        }

        public void setTemperatureCelsius(double temperatureCelsius) {
            this.temperatureCelsius = temperatureCelsius;
            this.temperatureFahrenheit = temperatureCelsius * 9 / 5 + 32;
        }

        public double getTemperatureFahrenheit() {
            return temperatureFahrenheit;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}

