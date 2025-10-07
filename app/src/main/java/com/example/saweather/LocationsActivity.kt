package com.example.saweather

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LocationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "📍 My Locations"
            textSize = 24f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 20)
        }

        // Sample locations
        val cities = listOf("Durban", "Johannesburg", "Cape Town", "Pretoria")

        cities.forEach { city ->
            val cityLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                setBackgroundColor(Color.parseColor("#F5F5F5"))
            }

            val tvCity = TextView(this).apply {
                text = city
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvTemp = TextView(this).apply {
                text = when(city) {
                    "Durban" -> "21°"
                    "Johannesburg" -> "22°"
                    "Cape Town" -> "18°"
                    "Pretoria" -> "23°"
                    else -> "N/A"
                }
                textSize = 18f
                setTextColor(Color.GRAY)
            }

            cityLayout.addView(tvCity)
            cityLayout.addView(tvTemp)

            cityLayout.setOnClickListener {
                Toast.makeText(this, "Selected: $city", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WeatherDetailActivity::class.java)
                startActivity(intent)
            }

            layout.addView(cityLayout)
        }

        val btnBack = Button(this).apply {
            text = "Back to Weather"
            setOnClickListener {
                finish()
            }
        }

        layout.addView(title)
        layout.addView(btnBack)
        setContentView(layout)
    }
}