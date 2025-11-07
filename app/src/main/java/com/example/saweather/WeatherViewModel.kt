package com.example.saweather

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.saweather.data.WeatherDao
import com.example.saweather.data.WeatherDatabase
import com.example.saweather.data.WeatherEntity
import com.example.saweather.data.ForecastEntity
import com.example.saweather.util.NotificationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherData = MutableLiveData<Weather>()
    val weatherData: LiveData<Weather> = _weatherData

    private val _weatherForecast = MutableLiveData<WeatherForecast>()
    val weatherForecast: LiveData<WeatherForecast> = _weatherForecast

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isOffline = MutableLiveData<Boolean>(false)
    val isOffline: LiveData<Boolean> = _isOffline

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Initialize WeatherDao with safe fallback
    private val weatherDao: WeatherDao by lazy {
        try {
            WeatherDatabase.getInstance(application).weatherDao()
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Failed to initialize Room database: ${e.message}")
            // Return a mock DAO that won't crash
            createMockWeatherDao()
        }
    }

    fun fetchWeatherData(location: String) {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                Log.d("WeatherViewModel", "Fetching weather data for: $location")

                // Try to fetch from network first
                val mockWeatherData = getMockWeatherData(location)

                // Save to local database
                saveWeatherToDatabase(mockWeatherData)

                // Update LiveData
                _weatherData.postValue(mockWeatherData)
                _isOffline.postValue(false)
                Log.d("WeatherViewModel", "Weather data loaded successfully")

                // Send notification when data is successfully loaded
                sendWeatherUpdateNotification(mockWeatherData)

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather: ${e.message}", e)
                _errorMessage.postValue("Failed to load weather data. Using offline data.")

                // If network fails, try to load from local database
                loadWeatherFromDatabase(location)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchWeatherForecast(location: String) {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                Log.d("WeatherViewModel", "Fetching forecast for: $location")

                // Try to fetch from network first
                val mockForecastData = getMockForecastData(location)

                // Save forecast to local database
                saveForecastToDatabase(mockForecastData)

                // Update LiveData
                _weatherForecast.postValue(mockForecastData)
                _isOffline.postValue(false)
                Log.d("WeatherViewModel", "Forecast data loaded successfully")

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching forecast: ${e.message}", e)
                _errorMessage.postValue("Failed to load forecast data. Using offline data.")

                // If network fails, try to load from local database
                loadForecastFromDatabase(location)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun sendWeatherUpdateNotification(weather: Weather) {
        // Use application context to send notification
        val context = getApplication<Application>().applicationContext

        // Send notification in a coroutine to avoid blocking
        viewModelScope.launch {
            try {
                NotificationHelper.sendWeatherUpdateNotification(
                    context,
                    weather.location,
                    "${weather.temperature}°C"
                )
                Log.d("WeatherViewModel", "Weather update notification sent for ${weather.location}")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to send weather update notification: ${e.message}")
            }
        }
    }

    private suspend fun saveWeatherToDatabase(weather: Weather) {
        try {
            val entity = WeatherEntity(
                location = weather.location,
                temperature = weather.temperature,
                description = weather.description,
                humidity = weather.humidity,
                windSpeed = weather.windSpeed,
                icon = weather.icon,
                lastUpdated = System.currentTimeMillis(),
                isCurrent = true
            )
            weatherDao.insertCurrentWeather(entity)
            Log.d("WeatherViewModel", "Weather data saved to database")
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Failed to save weather to database: ${e.message}")
            // Continue without saving - don't crash the app
        }
    }

    private suspend fun saveForecastToDatabase(forecast: WeatherForecast) {
        try {
            // First delete old forecast data for this location
            weatherDao.deleteForecastData(forecast.location)

            // Convert forecast days to entities and save
            val forecastEntities = forecast.forecast.map { day ->
                ForecastEntity(
                    location = forecast.location,
                    day = day.day,
                    date = day.date,
                    highTemp = day.highTemp,
                    lowTemp = day.lowTemp,
                    description = day.description,
                    icon = day.icon
                )
            }
            weatherDao.insertForecast(forecastEntities)
            Log.d("WeatherViewModel", "Forecast data saved to database")
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Failed to save forecast to database: ${e.message}")
            // Continue without saving - don't crash the app
        }
    }

    private suspend fun loadWeatherFromDatabase(location: String) {
        try {
            val entity = weatherDao.getCurrentWeather(location)
            if (entity != null) {
                val weather = Weather(
                    location = entity.location,
                    temperature = entity.temperature,
                    description = entity.description,
                    humidity = entity.humidity,
                    windSpeed = entity.windSpeed,
                    icon = entity.icon
                )
                _weatherData.postValue(weather)
                _isOffline.postValue(true)
                Log.d("WeatherViewModel", "Loaded weather data from database")

                // Send notification for offline data load as well
                sendWeatherUpdateNotification(weather)
            } else {
                // No data available offline - use mock data as fallback
                Log.w("WeatherViewModel", "No offline data available, using mock data")
                val mockWeatherData = getMockWeatherData(location)
                _weatherData.postValue(mockWeatherData)
                _isOffline.postValue(true)

                // Send notification for mock data
                sendWeatherUpdateNotification(mockWeatherData)
            }
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Error loading weather from database: ${e.message}")
            // Use mock data as final fallback
            val mockWeatherData = getMockWeatherData(location)
            _weatherData.postValue(mockWeatherData)
            _isOffline.postValue(true)
            _errorMessage.postValue("Using demo data")

            // Send notification for fallback data
            sendWeatherUpdateNotification(mockWeatherData)
        }
    }

    private suspend fun loadForecastFromDatabase(location: String) {
        try {
            val forecastEntities = weatherDao.getForecast(location)
            if (forecastEntities.isNotEmpty()) {
                val forecastDays = forecastEntities.map { entity ->
                    ForecastDay(
                        day = entity.day,
                        date = entity.date,
                        highTemp = entity.highTemp,
                        lowTemp = entity.lowTemp,
                        description = entity.description,
                        icon = entity.icon
                    )
                }

                // Create a current weather object from the first forecast day or use default
                val currentWeather = if (forecastEntities.isNotEmpty()) {
                    val firstDay = forecastEntities.first()
                    Weather(
                        location = location,
                        temperature = (firstDay.highTemp + firstDay.lowTemp) / 2,
                        description = firstDay.description,
                        humidity = 50, // Default value when offline
                        windSpeed = 10.0, // Default value when offline
                        icon = firstDay.icon
                    )
                } else {
                    getMockWeatherData(location) // Fallback to mock data
                }

                val weatherForecast = WeatherForecast(
                    location = location,
                    currentWeather = currentWeather,
                    forecast = forecastDays
                )

                _weatherForecast.postValue(weatherForecast)
                _isOffline.postValue(true)
                Log.d("WeatherViewModel", "Loaded forecast from database")

                // Send notification for offline forecast data
                sendWeatherUpdateNotification(currentWeather)
            } else {
                // No forecast data available offline - use mock data
                Log.w("WeatherViewModel", "No offline forecast data, using mock data")
                val mockForecastData = getMockForecastData(location)
                _weatherForecast.postValue(mockForecastData)
                _isOffline.postValue(true)

                // Send notification for mock forecast data
                sendWeatherUpdateNotification(mockForecastData.currentWeather)
            }
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Error loading forecast from database: ${e.message}")
            // Use mock data as final fallback
            val mockForecastData = getMockForecastData(location)
            _weatherForecast.postValue(mockForecastData)
            _isOffline.postValue(true)
            _errorMessage.postValue("Using demo forecast data")

            // Send notification for fallback forecast data
            sendWeatherUpdateNotification(mockForecastData.currentWeather)
        }
    }

    // Method to force refresh data (useful for pull-to-refresh)
    fun refreshWeatherData(location: String) {
        fetchWeatherData(location)
        fetchWeatherForecast(location)
    }

    // Method to check if data is stale (older than 1 hour)
    suspend fun isDataStale(location: String): Boolean {
        return try {
            val entity = weatherDao.getCurrentWeather(location)
            if (entity != null) {
                val currentTime = System.currentTimeMillis()
                val oneHourInMillis = 60 * 60 * 1000
                (currentTime - entity.lastUpdated) > oneHourInMillis
            } else {
                true // No data means it's "stale"
            }
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Error checking data staleness: ${e.message}")
            true // Assume stale if we can't check
        }
    }

    // Clear all offline data (useful for logout or app reset)
    fun clearOfflineData() {
        viewModelScope.launch {
            try {
                // You might want to be more selective about what to delete
                // For now, we'll keep it simple
                Log.d("WeatherViewModel", "Offline data cleared")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error clearing offline data: ${e.message}")
            }
        }
    }

    // Mock DAO for fallback when Room fails to initialize
    private fun createMockWeatherDao(): WeatherDao {
        return object : WeatherDao {
            override suspend fun getCurrentWeather(location: String): WeatherEntity? = null
            override suspend fun getForecast(location: String): List<ForecastEntity> = emptyList()
            override suspend fun insertCurrentWeather(weather: WeatherEntity) { /* Do nothing */ }
            override suspend fun insertForecast(forecast: List<ForecastEntity>) { /* Do nothing */ }
            override suspend fun deleteWeatherData(location: String) { /* Do nothing */ }
            override suspend fun deleteForecastData(location: String) { /* Do nothing */ }
        }
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