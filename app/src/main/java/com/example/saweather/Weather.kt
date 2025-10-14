package com.example.saweather

data class Weather(
    val location: String = "",
    val temperature: Double = 0.0,
    val description: String = "",
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val icon: String = ""
)

data class ForecastDay(
    val day: String = "",
    val date: String = "",
    val highTemp: Double = 0.0,
    val lowTemp: Double = 0.0,
    val description: String = "",
    val icon: String = ""
)

data class WeatherForecast(
    val location: String = "",
    val currentWeather: Weather = Weather(),
    val forecast: List<ForecastDay> = emptyList()
)