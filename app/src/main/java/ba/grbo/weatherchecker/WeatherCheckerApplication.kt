package ba.grbo.weatherchecker

import android.app.Application
import ba.grbo.weatherchecker.util.Trees
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import timber.log.Timber

@HiltAndroidApp
class WeatherCheckerApplication : Application() {
    val scope = MainScope()

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        Timber.plant(if (BuildConfig.DEBUG) Trees.Debug else Trees.Release)
    }


    override fun onLowMemory() {
        super.onLowMemory()
        scope.cancel()
    }
}