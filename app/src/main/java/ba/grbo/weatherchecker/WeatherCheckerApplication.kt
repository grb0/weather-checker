package ba.grbo.weatherchecker

import android.app.Application
import ba.grbo.weatherchecker.util.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import javax.inject.Inject

@HiltAndroidApp
class WeatherCheckerApplication : Application() {
    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var logger: Logger

    override fun onCreate() {
        super.onCreate()
        logger.init()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        scope.cancel()
    }
}