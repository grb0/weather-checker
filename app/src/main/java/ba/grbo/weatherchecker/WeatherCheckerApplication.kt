package ba.grbo.weatherchecker

import android.app.Application
import ba.grbo.weatherchecker.util.DefaultLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherCheckerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DefaultLogger.init()
    }
}