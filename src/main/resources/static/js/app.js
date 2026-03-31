const cityInput = document.getElementById("cityInput");
const searchButton = document.getElementById("searchButton");
const errorBox = document.getElementById("errorBox");

const currentSection = document.getElementById("currentWeather");
const forecastSection = document.getElementById("forecastSection");

const locationEl = document.getElementById("location");
const conditionEl = document.getElementById("condition");
const tempCEl = document.getElementById("tempC");
const tempFEl = document.getElementById("tempF");
const humidityEl = document.getElementById("humidity");
const timestampEl = document.getElementById("timestamp");
const iconEl = document.getElementById("icon");

const forecastListEl = document.getElementById("forecastList");

async function fetchJson(url) {
    const response = await fetch(url);
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        const message = data.error || `Request failed with status ${response.status}`;
        throw new Error(message);
    }
    return response.json();
}

function formatDateTime(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    return d.toLocaleString(undefined, {
        weekday: "short",
        hour: "2-digit",
        minute: "2-digit",
    });
}

function createForecastCard(entry) {
    const div = document.createElement("div");
    div.className = "forecast-item";

    const date = document.createElement("h4");
    date.textContent = formatDateTime(entry.dateTime);

    const temp = document.createElement("p");
    temp.textContent = `${entry.temperatureCelsius.toFixed(1)} °C / ${entry.temperatureFahrenheit.toFixed(1)} °F`;

    const humidity = document.createElement("p");
    humidity.textContent = `Humidity: ${entry.humidity}%`;

    const condition = document.createElement("p");
    condition.textContent = entry.condition;

    const icon = document.createElement("img");
    if (entry.icon) {
        icon.src = `https://openweathermap.org/img/wn/${entry.icon}@2x.png`;
        icon.alt = entry.condition || "Weather icon";
    }

    div.appendChild(date);
    div.appendChild(icon);
    div.appendChild(temp);
    div.appendChild(humidity);
    div.appendChild(condition);

    return div;
}

async function search() {
    const city = cityInput.value.trim();
    if (!city) {
        return;
    }

    errorBox.classList.add("hidden");
    currentSection.classList.add("hidden");
    forecastSection.classList.add("hidden");
    forecastListEl.innerHTML = "";

    try {
        const [current, forecast] = await Promise.all([
            fetchJson(`/api/weather/current?city=${encodeURIComponent(city)}`),
            fetchJson(`/api/weather/forecast?city=${encodeURIComponent(city)}`),
        ]);

        // Current
        locationEl.textContent = `${current.city || city}, ${current.country || ""}`.trim();
        conditionEl.textContent = current.condition || "";
        tempCEl.textContent = current.temperatureCelsius.toFixed(1);
        tempFEl.textContent = current.temperatureFahrenheit.toFixed(1);
        humidityEl.textContent = current.humidity;
        timestampEl.textContent = current.timestamp
            ? new Date(current.timestamp).toLocaleString()
            : "";

        if (current.icon) {
            iconEl.src = `https://openweathermap.org/img/wn/${current.icon}@4x.png`;
            iconEl.alt = current.condition || "Weather icon";
        } else {
            iconEl.removeAttribute("src");
            iconEl.alt = "";
        }

        currentSection.classList.remove("hidden");

        // Forecast
        if (forecast && Array.isArray(forecast.forecast)) {
            forecast.forecast.forEach((entry) => {
                forecastListEl.appendChild(createForecastCard(entry));
            });
            if (forecast.forecast.length > 0) {
                forecastSection.classList.remove("hidden");
            }
        }
    } catch (err) {
        errorBox.textContent = err.message;
        errorBox.classList.remove("hidden");
    }
}

searchButton.addEventListener("click", search);
cityInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        search();
    }
});

