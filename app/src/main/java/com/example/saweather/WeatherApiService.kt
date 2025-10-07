package com.example.saweather

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") airQuality: String = "no"
    ): Response<CurrentWeatherResponse>

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 5,
        @Query("aqi") airQuality: String = "no",
        @Query("alerts") alerts: String = "no"
    ): Response<ForecastResponse>

    @GET("search.json")
    suspend fun searchLocations(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): Response<List<LocationSearchResponse>>
}