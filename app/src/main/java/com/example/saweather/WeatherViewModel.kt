package com.example.saweather

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableLiveData<Weather>()
    val weatherData: LiveData<Weather> = _weatherData

    private val _weatherForecast = MutableLiveData<WeatherForecast>()
    val weatherForecast: LiveData<WeatherForecast> = _weatherForecast

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchWeatherData(location: String) {
        _isLoading.value = true

        // Simulate API call
        val mockWeatherData = getMockWeatherData(location)

        // Simulate network delay
        Handler(Looper.getMainLooper()).postDelayed({
            _weatherData.value = mockWeatherData
            _isLoading.value = false
        }, 1000)
    }

    fun fetchWeatherForecast(location: String) {
        _isLoading.value = true

        // Simulate API call for forecast
        val mockForecastData = getMockForecastData(location)

        // Simulate network delay
        Handler(Looper.getMainLooper()).postDelayed({
            _weatherForecast.value = mockForecastData
            _isLoading.value = false
        }, 1000)
    }

    private fun getMockWeatherData(location: String): Weather {
        // Your existing mock data
        return when (location.lowercase()) {
            "johannesburg" -> Weather(
                location = "Johannesburg",
                temperature = 22.0,
                description = "Sunny",
                humidity = 45,
                windSpeed = 15.0,
                icon = "☀️"
            )
            "cape town" -> Weather(
                location = "Cape Town",
                temperature = 18.0,
                description = "Cloudy",
                humidity = 65,
                windSpeed = 25.0,
                icon = "☁️"
            )
            "durban" -> Weather(
                location = "Durban",
                temperature = 26.0,
                description = "Partly Cloudy",
                humidity = 75,
                windSpeed = 12.0,
                icon = "⛅"
            )
            "pretoria" -> Weather(
                location = "Pretoria",
                temperature = 24.0,
                description = "Clear",
                humidity = 50,
                windSpeed = 10.0,
                icon = "☀️"
            )
            "port elizabeth" -> Weather(
                location = "Port Elizabeth",
                temperature = 20.0,
                description = "Windy",
                humidity = 60,
                windSpeed = 30.0,
                icon = "💨"
            )
            "bloemfontein" -> Weather(
                location = "Bloemfontein",
                temperature = 19.0,
                description = "Sunny",
                humidity = 40,
                windSpeed = 18.0,
                icon = "☀️"
            )
            else -> Weather(
                location = location,
                temperature = 20.0,
                description = "Clear",
                humidity = 50,
                windSpeed = 10.0,
                icon = "☀️"
            )
        }
    }

    private fun getMockForecastData(location: String): WeatherForecast {
        val currentWeather = getMockWeatherData(location)
        val forecastDays = generateMockForecast(location)

        return WeatherForecast(
            location = location,
            currentWeather = currentWeather,
            forecast = forecastDays
        )
    }

    private fun generateMockForecast(location: String): List<ForecastDay> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val daysFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val forecast = mutableListOf<ForecastDay>()

        // Base temperatures based on location
        val baseTemp = when (location.lowercase()) {
            "johannesburg" -> 22.0
            "cape town" -> 18.0
            "durban" -> 26.0
            "pretoria" -> 24.0
            "port elizabeth" -> 20.0
            "bloemfontein" -> 19.0
            else -> 20.0
        }

        // Weather patterns based on location
        val weatherPatterns = when (location.lowercase()) {
            "cape town" -> listOf("☁️ Cloudy", "🌧️ Rainy", "⛅ Partly Cloudy", "💨 Windy", "☀️ Sunny")
            "durban" -> listOf("⛅ Partly Cloudy", "☀️ Sunny", "🌧️ Showers", "☀️ Sunny", "⛅ Partly Cloudy")
            else -> listOf("☀️ Sunny", "⛅ Partly Cloudy", "☀️ Sunny", "☁️ Cloudy", "⛅ Partly Cloudy")
        }

        for (i in 1..5) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            val dayName = daysFormat.format(calendar.time)
            val date = dateFormat.format(calendar.time)

            // Generate realistic temperature variations
            val tempVariation = (i - 3) * 2 // Slight temperature curve
            val highTemp = baseTemp + tempVariation + (0..3).random()
            val lowTemp = highTemp - (5..8).random()

            val weatherIndex = (i - 1) % weatherPatterns.size
            val weatherPattern = weatherPatterns[weatherIndex]

            forecast.add(
                ForecastDay(
                    day = dayName,
                    date = date,
                    highTemp = highTemp,
                    lowTemp = lowTemp,
                    description = weatherPattern,
                    icon = weatherPattern.substringBefore(" ")
                )
            )
        }

        return forecast
    }
}