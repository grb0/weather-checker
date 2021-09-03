package ba.grbo.weatherchecker.util

import ba.grbo.weatherchecker.BuildConfig
import ba.grbo.weatherchecker.util.Tree.Companion.DEBUG_TREE
import ba.grbo.weatherchecker.util.Tree.Companion.RELEASE_TREE
import timber.log.Timber

interface Logger {
    fun init()

    fun d(message: String)

    fun e(throwable: Throwable)
}

object DefaultLogger : Logger {
    override fun init() {
        Timber.plant(if (BuildConfig.DEBUG) Timber.DEBUG_TREE else Timber.RELEASE_TREE)
    }

    override fun d(message: String) {
        Timber.d(message)
    }

    override fun e(throwable: Throwable) {
        Timber.e(throwable)
    }
}