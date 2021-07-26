package ba.grbo.weatherchecker.ui.activities

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import ba.grbo.weatherchecker.R

class WeatherCheckerActivity : AppCompatActivity() {
    var onScreenTouchedListener: ((event: MotionEvent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WeatherChecker)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_checker)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) onScreenTouchedListener?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }
}