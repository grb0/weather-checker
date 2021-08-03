package ba.grbo.weatherchecker

import android.app.Application
import ba.grbo.weatherchecker.util.CustomFormatStrategy
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

@HiltAndroidApp
class WeatherCheckerApplication : Application() {
    val scope = MainScope()

    override fun onCreate() {
        super.onCreate()
        initializeLogger()
    }

    private fun initializeLogger() {
        if (BuildConfig.DEBUG) Logger.addLogAdapter(AndroidLogAdapter(CustomFormatStrategy()))
    }

    override fun onLowMemory() {
        super.onLowMemory()
        scope.cancel()
    }
}