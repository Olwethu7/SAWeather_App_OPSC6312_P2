package com.example.saweather

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AirQualityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "🌫️ Air Quality - Durban"
            textSize = 24f
        }

        val aqi = TextView(this).apply {
            text = "Air Quality Index: 56 - Moderate"
            textSize = 18f
            setPadding(0, 16, 0, 0)
        }

        val description = TextView(this).apply {
            text = "Air quality is acceptable. Unusually sensitive people should consider reducing prolonged or heavy outdoor exertion."
            textSize = 14f
            setPadding(0, 8, 0, 0)
        }

        layout.addView(title)
        layout.addView(aqi)
        layout.addView(description)
        setContentView(layout)
    }
}