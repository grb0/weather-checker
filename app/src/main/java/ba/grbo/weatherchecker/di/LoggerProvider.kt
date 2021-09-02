package ba.grbo.weatherchecker.di

import ba.grbo.weatherchecker.BuildConfig
import ba.grbo.weatherchecker.util.Logger
import ba.grbo.weatherchecker.util.Tree.Companion.DEBUG_TREE
import ba.grbo.weatherchecker.util.Tree.Companion.createReleaseTree
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LoggerProvider {
    @Singleton
    @Provides
    fun provideLogger(
        scope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher
    ): Logger = object : Logger {
        override fun init() {
            Timber.plant(
                if (BuildConfig.DEBUG) Timber.DEBUG_TREE
                else Timber.createReleaseTree(scope, ioDispatcher)
            )
        }
    }
}