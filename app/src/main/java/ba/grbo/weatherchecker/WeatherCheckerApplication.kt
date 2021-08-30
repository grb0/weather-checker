package ba.grbo.weatherchecker

import android.app.Application
import ba.grbo.weatherchecker.util.Tree.Companion.DEBUG_TREE
import ba.grbo.weatherchecker.util.Tree.Companion.RELEASE_TREE
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

    override fun onLowMemory() {
        super.onLowMemory()
        scope.cancel()
    }

    private fun initTimber() {
        Timber.plant(if (BuildConfig.DEBUG) Timber.DEBUG_TREE else Timber.RELEASE_TREE)
    }
}