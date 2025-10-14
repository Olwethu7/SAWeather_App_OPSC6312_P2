package com.example.saweather

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var preferences: Preferences
    private lateinit var viewModel: WeatherViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language BEFORE super.onCreate
        languageManager = LanguageManager(this)
        languageManager.updateConfiguration(this) // Changed from applyLanguage to updateConfiguration

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        auth = Firebase.auth
        preferences = Preferences(this)
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        // Check if user is logged in
        if (!preferences.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        setupObservers()
        setupForecastObservers()
        loadWeatherData()
        loadWeatherForecast()
        updateAllTexts() // Update all texts including welcome
    }

    private fun initViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView)
        locationTextView = findViewById(R.id.locationTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        windTextView = findViewById(R.id.windTextView)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun updateAllTexts() {
        // Update welcome message
        updateWelcomeMessage()

        // Update other static texts if any
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun updateWelcomeMessage() {
        val userName = preferences.getCurrentUserName()
        val welcomeText = getString(R.string.welcome)

        if (!userName.isNullOrEmpty()) {
            "$welcomeText, $userName! 👋".also { welcomeTextView.text = it }
        } else {
            val currentUser = auth.currentUser
            if (currentUser?.email != null) {
                val nameFromEmail = currentUser.email!!.substringBefore("@")
                "$welcomeText, $nameFromEmail! 👋".also { welcomeTextView.text = it }
            } else {
                "$welcomeText to SAWeather! 🌤️".also { welcomeTextView.text = it }
            }
        }
    }

    private fun setupObservers() {
        viewModel.weatherData.observe(this) { weather ->
            if (weather != null) {
                locationTextView.text = weather.location
                "${weather.temperature}°C".also { temperatureTextView.text = it }
                descriptionTextView.text = weather.description
                "${getString(R.string.humidity)}: ${weather.humidity}%".also { humidityTextView.text = it }
                "${getString(R.string.wind)}: ${weather.windSpeed} km/h".also { windTextView.text = it }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupForecastObservers() {
        viewModel.weatherForecast.observe(this) { forecast ->
            if (forecast != null) {
                // Update current weather
                val current = forecast.currentWeather
                locationTextView.text = current.location
                "${current.temperature}°C".also { temperatureTextView.text = it }
                descriptionTextView.text = current.description
                "${getString(R.string.humidity)}: ${current.humidity}%".also { humidityTextView.text = it }
                "${getString(R.string.wind)}: ${current.windSpeed} km/h".also { windTextView.text = it }

                // Update forecast
                displayForecast(forecast.forecast)
            }
        }
    }

    private fun displayForecast(forecast: List<ForecastDay>) {
        val forecastContainer = findViewById<LinearLayout>(R.id.forecastContainer)
        forecastContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        forecast.forEach { day ->
            val forecastItemView = inflater.inflate(R.layout.item_forecast, forecastContainer, false)

            val dayTextView = forecastItemView.findViewById<TextView>(R.id.dayTextView)
            val dateTextView = forecastItemView.findViewById<TextView>(R.id.dateTextView)
            val iconTextView = forecastItemView.findViewById<TextView>(R.id.iconTextView)
            val highTempTextView = forecastItemView.findViewById<TextView>(R.id.highTempTextView)
            val lowTempTextView = forecastItemView.findViewById<TextView>(R.id.lowTempTextView)

            dayTextView.text = day.day
            dateTextView.text = day.date
            iconTextView.text = day.icon
            "${day.highTemp.toInt()}°".also { highTempTextView.text = it } // Fixed: use highTemp
            "${day.lowTemp.toInt()}°".also { lowTempTextView.text = it }   // Fixed: use lowTemp

            forecastContainer.addView(forecastItemView)
        }
    }

    private fun loadWeatherData() {
        val location = preferences.getCurrentUserLocation()
        viewModel.fetchWeatherData(location)
    }

    private fun loadWeatherForecast() {
        val location = preferences.getCurrentUserLocation()
        viewModel.fetchWeatherForecast(location)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_change_language -> {
                showLanguageDialog()
                true
            }
            R.id.action_change_location -> {
                showChangeLocationDialog()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            "English", // Use hardcoded for now
            "Zulu (isiZulu)",
            "Afrikaans"
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.select_language)
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> setAppLanguage(LanguageManager.LANGUAGE_ENGLISH)
                    1 -> setAppLanguage(LanguageManager.LANGUAGE_ZULU)
                    2 -> setAppLanguage(LanguageManager.LANGUAGE_AFRIKAANS)
                }
            }
            .show()
    }

    private fun setAppLanguage(languageCode: String) {
        languageManager.setLanguage(languageCode)
        // Force complete recreation with new language
        recreate()
    }

    private fun showChangeLocationDialog() {
        val locations = arrayOf("Johannesburg", "Cape Town", "Durban", "Pretoria", "Port Elizabeth", "Bloemfontein")

        AlertDialog.Builder(this)
            .setTitle(R.string.change_location)
            .setItems(locations) { _, which ->
                val selectedLocation = locations[which]
                preferences.updateLocation(selectedLocation)
                viewModel.fetchWeatherData(selectedLocation)
                viewModel.fetchWeatherForecast(selectedLocation)
            }
            .show()
    }

    private fun logout() {
        auth.signOut()
        preferences.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Update texts when configuration changes (including language)
        updateAllTexts()
    }

    override fun onResume() {
        super.onResume()
        // Update all texts when returning to activity
        updateAllTexts()
    }
}