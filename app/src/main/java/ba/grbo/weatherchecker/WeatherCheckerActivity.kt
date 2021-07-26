package ba.grbo.weatherchecker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WeatherCheckerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WeatherChecker)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_checker)
    }
}