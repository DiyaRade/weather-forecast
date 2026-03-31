# Weather Forecast (Spring Boot)

Full-stack Weather Forecast application using **Java + Spring Boot** with a simple **HTML/CSS/JS** UI.

## Features

- Current weather (temperature, humidity, condition)
- 5-day forecast (OpenWeather 3-hour list)
- Weather icons
- Basic caching (Caffeine) to reduce API calls
- Error handling for invalid cities / API key issues

## Prerequisites

- Java 17+
- Maven 3.9+
- OpenWeatherMap API key

## Configure API Key (Recommended)

Set environment variable `OPENWEATHER_API_KEY`.

### Windows PowerShell

```powershell
$env:OPENWEATHER_API_KEY="YOUR_KEY_HERE"
```

Then run the app in the same terminal session.

## Run

```bash
mvn spring-boot:run
```

Open:
- `http://localhost:8080`

## REST API

- `GET /api/weather/current?city=London`
- `GET /api/weather/forecast?city=London`

