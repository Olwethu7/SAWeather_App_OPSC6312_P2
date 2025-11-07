package com.example.saweather.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherEntity(
    @PrimaryKey
    val location: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val icon: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = true
)

@Entity(tableName = "forecast_data")
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val location: String,
    val day: String,
    val date: String,
    val highTemp: Double,
    val lowTemp: Double,
    val description: String,
    val icon: String
)