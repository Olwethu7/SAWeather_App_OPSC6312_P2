package com.example.saweather

import android.util.Log

class WeatherRepository {
    private val TAG = "WeatherRepository"
    private val apiService = RetrofitClient.getWeatherService()

    suspend fun getCurrentWeather(location: String = "Durban"): WeatherResult<CurrentWeatherResponse> {
        return try {
            Log.d(TAG, "Fetching current weather for: $location")
            val response = apiService.getCurrentWeather(
                apiKey = AppConfig.WEATHER_API_KEY,
                location = location
            )

            if (response.isSuccessful) {
                response.body()?.let { weatherData ->
                    Log.d(TAG, "Weather data received: ${weatherData.location.name}")
                    WeatherResult.Success(weatherData)
                } ?: run {
                    Log.e(TAG, "Response body is null")
                    WeatherResult.Error("No weather data received")
                }
            } else {
                val errorMsg = "API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                WeatherResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Network error: ${e.message}"
            Log.e(TAG, errorMsg, e)
            WeatherResult.Error(errorMsg)
        }
    }

    suspend fun getForecast(location: String = "Durban", days: Int = 5): WeatherResult<ForecastResponse> {
        return try {
            Log.d(TAG, "Fetching forecast for: $location, days: $days")
            val response = apiService.getForecast(
                apiKey = AppConfig.WEATHER_API_KEY,
                location = location,
                days = days
            )

            if (response.isSuccessful) {
                response.body()?.let { forecastData ->
                    Log.d(TAG, "Forecast data received: ${forecastData.location.name}")
                    WeatherResult.Success(forecastData)
                } ?: run {
                    Log.e(TAG, "Forecast response body is null")
                    WeatherResult.Error("No forecast data received")
                }
            } else {
                val errorMsg = "Forecast API Error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                WeatherResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Forecast network error: ${e.message}"
            Log.e(TAG, errorMsg, e)
            WeatherResult.Error(errorMsg)
        }
    }

    suspend fun searchLocations(query: String): WeatherResult<List<LocationSearchResponse>> {
        return try {
            val response = apiService.searchLocations(
                apiKey = AppConfig.WEATHER_API_KEY,
                query = query
            )

            if (response.isSuccessful) {
                response.body()?.let { locations ->
                    WeatherResult.Success(locations)
                } ?: run {
                    WeatherResult.Error("No locations found")
                }
            } else {
                WeatherResult.Error("Location search failed: ${response.code()}")
            }
        } catch (e: Exception) {
            WeatherResult.Error("Location search error: ${e.message}")
        }
    }

    // Simple API test
    suspend fun testApiConnection(): Boolean {
        return try {
            val response = apiService.getCurrentWeather(
                apiKey = AppConfig.WEATHER_API_KEY,
                location = "Durban"
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

// Simple result sealed class
sealed class WeatherResult<out T> {
    data class Success<out T>(val data: T) : WeatherResult<T>()
    data class Error(val message: String) : WeatherResult<Nothing>()
}