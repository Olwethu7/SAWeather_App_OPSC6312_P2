package com.example.saweather

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.saweather.databinding.ActivityWeatherDetailBinding
import kotlinx.coroutines.launch

class WeatherDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherDetailBinding
    private val TAG = "WeatherDetailActivity"
    private val auth = Firebase.auth
    private val weatherRepository = WeatherRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityWeatherDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Log.d(TAG, "WeatherDetailActivity created")
            checkUserAuthentication()
            setupClickListeners()
            loadWeatherData()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            setDefaultWeatherData()
        }
    }

    private fun loadWeatherData() {
        lifecycleScope.launch {
            try {
                showLoadingState()

                when (val result = weatherRepository.getCurrentWeather()) {
                    is WeatherResult.Success -> {
                        val weatherData = result.data
                        updateWeatherUI(weatherData)
                        Toast.makeText(this@WeatherDetailActivity, "Weather updated! ✅", Toast.LENGTH_SHORT).show()
                    }
                    is WeatherResult.Error -> {
                        Log.e(TAG, "Weather API error: ${result.message}")
                        Toast.makeText(this@WeatherDetailActivity, "Weather update failed", Toast.LENGTH_LONG).show()
                        setDefaultWeatherData()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading weather: ${e.message}", e)
                setDefaultWeatherData()
            }
        }
    }

    private fun updateWeatherUI(weatherData: CurrentWeatherResponse) {
        binding.tvLocation.text = "${weatherData.location.name}, ${weatherData.location.country}"
        binding.tvTemperature.text = "${weatherData.current.tempC.toInt()}°C"
        binding.tvCondition.text = weatherData.current.condition.text
        binding.tvHumidity.text = "💧 Humidity: ${weatherData.current.humidity}%"
        binding.tvWind.text = "💨 Wind: ${weatherData.current.windKph.toInt()} km/h"
    }

    private fun showLoadingState() {
        binding.tvLocation.text = "Loading..."
        binding.tvTemperature.text = "--°C"
        binding.tvCondition.text = "Getting weather data..."
    }

    private fun setDefaultWeatherData() {
        binding.tvLocation.text = "Durban, South Africa"
        binding.tvTemperature.text = "21°C"
        binding.tvCondition.text = "Partly Cloudy"
        binding.tvHumidity.text = "💧 Humidity: 73%"
        binding.tvWind.text = "💨 Wind: 5 km/h"
    }

    private fun checkUserAuthentication() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            redirectToLogin()
            return
        }
        binding.tvUserEmail.text = "Logged in as: ${currentUser.email}"
    }

    private fun setupClickListeners() {
        binding.btnLocations.setOnClickListener {
            Toast.makeText(this, "Locations feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnForecast.setOnClickListener {
            Toast.makeText(this, "Forecast feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnAirQuality.setOnClickListener {
            Toast.makeText(this, "Air Quality feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnRefresh.setOnClickListener {
            loadWeatherData()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        try {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Logout error: ${e.message}")
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }

    private fun createFallbackLayout() {
        // Fallback implementation
    }
}