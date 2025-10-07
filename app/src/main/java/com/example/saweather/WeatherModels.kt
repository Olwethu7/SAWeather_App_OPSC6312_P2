package com.example.saweather

import com.google.gson.annotations.SerializedName

// Current Weather Response
data class CurrentWeatherResponse(
    @SerializedName("location") val location: Location,
    @SerializedName("current") val current: CurrentWeather
)

// Forecast Response
data class ForecastResponse(
    @SerializedName("location") val location: Location,
    @SerializedName("current") val current: CurrentWeather,
    @SerializedName("forecast") val forecast: Forecast
)

// Location Search Response
data class LocationSearchResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("region") val region: String,
    @SerializedName("country") val country: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("url") val url: String
)

// Location Data
data class Location(
    @SerializedName("name") val name: String,
    @SerializedName("region") val region: String,
    @SerializedName("country") val country: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("localtime") val localTime: String
)

// Current Weather Data
data class CurrentWeather(
    @SerializedName("last_updated") val lastUpdated: String,
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("temp_f") val tempF: Double,
    @SerializedName("condition") val condition: WeatherCondition,
    @SerializedName("wind_kph") val windKph: Double,
    @SerializedName("wind_dir") val windDir: String,
    @SerializedName("pressure_mb") val pressureMb: Double,
    @SerializedName("precip_mm") val precipMm: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("cloud") val cloud: Int,
    @SerializedName("feelslike_c") val feelsLikeC: Double,
    @SerializedName("feelslike_f") val feelsLikeF: Double,
    @SerializedName("vis_km") val visKm: Double,
    @SerializedName("uv") val uv: Double,
    @SerializedName("gust_kph") val gustKph: Double
) {
    // Helper functions
    fun getFormattedTemperature(): String = "${tempC.toInt()}°C"
    fun getFormattedFeelsLike(): String = "${feelsLikeC.toInt()}°C"
    fun getFormattedHumidity(): String = "$humidity%"
    fun getFormattedWind(): String = "${windKph.toInt()} km/h"
    fun getFormattedVisibility(): String = "${visKm.toInt()} km"
}

// Weather Condition
data class WeatherCondition(
    @SerializedName("text") val text: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("code") val code: Int
) {
    // Get full icon URL (WeatherAPI.com returns relative URLs starting with //)
    fun getIconUrl(): String = "https:${icon}"
}

// Forecast Data
data class Forecast(
    @SerializedName("forecastday") val forecastDays: List<ForecastDay>
)

data class ForecastDay(
    @SerializedName("date") val date: String,
    @SerializedName("date_epoch") val dateEpoch: Long,
    @SerializedName("day") val day: DayForecast,
    @SerializedName("astro") val astro: Astro,
    @SerializedName("hour") val hours: List<HourForecast>
)

data class DayForecast(
    @SerializedName("maxtemp_c") val maxTempC: Double,
    @SerializedName("mintemp_c") val minTempC: Double,
    @SerializedName("avgtemp_c") val avgTempC: Double,
    @SerializedName("maxwind_kph") val maxWindKph: Double,
    @SerializedName("totalprecip_mm") val totalPrecipMm: Double,
    @SerializedName("avgvis_km") val avgVisKm: Double,
    @SerializedName("avghumidity") val avgHumidity: Int,
    @SerializedName("condition") val condition: WeatherCondition,
    @SerializedName("uv") val uv: Double
) {
    fun getFormattedHighTemp(): String = "${maxTempC.toInt()}°C"
    fun getFormattedLowTemp(): String = "${minTempC.toInt()}°C"
}

data class Astro(
    @SerializedName("sunrise") val sunrise: String,
    @SerializedName("sunset") val sunset: String,
    @SerializedName("moonrise") val moonrise: String,
    @SerializedName("moonset") val moonset: String
)

data class HourForecast(
    @SerializedName("time") val time: String,
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("condition") val condition: WeatherCondition,
    @SerializedName("wind_kph") val windKph: Double,
    @SerializedName("chance_of_rain") val chanceOfRain: Int
)

// ForecastItem for compatibility
data class ForecastItem(
    val day: String,
    val highTemp: String,
    val lowTemp: String,
    val icon: String
)

// Extension function to convert to your existing format
fun ForecastDay.toForecastItem(): ForecastItem {
    return ForecastItem(
        day = getFormattedDay(),
        highTemp = day.getFormattedHighTemp(),
        lowTemp = day.getFormattedLowTemp(),
        icon = day.condition.getIconUrl()
    )
}

private fun ForecastDay.getFormattedDay(): String {
    return try {
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(date)
        val dayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
        dayFormat.format(date)
    } catch (e: Exception) {
        "Unknown"
    }
}

// Result wrapper for API calls
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}