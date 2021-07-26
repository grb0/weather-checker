package ba.grbo.weatherchecker

import android.app.Application
import timber.log.Timber

class WeatherCheckerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeTimber()
    }

    private fun initializeTimber() {
        val tree: Timber.DebugTree by lazy {
            object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, "ba.grbo -> $tag", message, t)
                }

                override fun createStackElementTag(
                    element: StackTraceElement
                ) = "${super.createStackElementTag(element)} -> ${element.methodName}"
            }
        }

        if (BuildConfig.DEBUG) Timber.plant(tree)
    }
}