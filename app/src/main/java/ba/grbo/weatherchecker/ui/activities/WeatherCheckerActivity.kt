package ba.grbo.weatherchecker.ui.activities

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ba.grbo.weatherchecker.R
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeatherCheckerActivity : AppCompatActivity() {
    private val viewModel: WeatherCheckerViewModel by viewModels()
    var onScreenTouchedListener: ((event: MotionEvent) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WeatherChecker)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_checker)
        viewModel.collectFlows()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) onScreenTouchedListener?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun WeatherCheckerViewModel.collectFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                internetStatus.collectLatest { hasInternet ->
                    // For smooth transition from wifi to cellular, to avoid showing no connection
                    // for a brief moment.
                    delay(300)
                    // Consume hasInternet
                }
            }
        }
    }
}