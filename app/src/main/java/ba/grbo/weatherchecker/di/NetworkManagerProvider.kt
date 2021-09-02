package ba.grbo.weatherchecker.di

import android.app.Application
import ba.grbo.weatherchecker.util.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkManagerProvider {
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope = MainScope()

    @Singleton
    @Provides
    fun provideNetworkManager(
        application: Application,
        scope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher
    ): NetworkManager {
        return NetworkManager(application, ioDispatcher, scope)
    }
}