package ba.grbo.weatherchecker.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ba.grbo.weatherchecker.R

class WeatherCheckerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WeatherChecker)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_checker)
    }
}