package com.example.saweather.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_data WHERE location = :location AND isCurrent = 1")
    suspend fun getCurrentWeather(location: String): WeatherEntity?

    @Query("SELECT * FROM forecast_data WHERE location = :location ORDER BY date ASC")
    suspend fun getForecast(location: String): List<ForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weather: WeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: List<ForecastEntity>)

    @Query("DELETE FROM weather_data WHERE location = :location")
    suspend fun deleteWeatherData(location: String)

    @Query("DELETE FROM forecast_data WHERE location = :location")
    suspend fun deleteForecastData(location: String)
}