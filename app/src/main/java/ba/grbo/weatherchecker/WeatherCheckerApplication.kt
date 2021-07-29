package ba.grbo.weatherchecker

import android.app.Application
import ba.grbo.weatherchecker.util.CustomFormatStrategy
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherCheckerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeLogger()
    }

    private fun initializeLogger() {
        if (BuildConfig.DEBUG) Logger.addLogAdapter(AndroidLogAdapter(CustomFormatStrategy()))
    }
}