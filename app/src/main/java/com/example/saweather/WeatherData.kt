package com.example.saweather

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeather,
    @SerializedName("daily") val daily: List<DailyWeather>
)

data class CurrentWeather(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("uvi") val uvIndex: Double,
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("weather") val weather: List<WeatherDescription>
)

data class DailyWeather(
    @SerializedName("dt") val date: Long,
    @SerializedName("temp") val temperature: Temperature,
    @SerializedName("weather") val weather: List<WeatherDescription>
)

data class WeatherDescription(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String
)

data class Temperature(
    @SerializedName("min") val min: Double,
    @SerializedName("max") val max: Double
)