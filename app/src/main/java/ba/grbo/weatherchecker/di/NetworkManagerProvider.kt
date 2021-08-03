package ba.grbo.weatherchecker.di

import android.app.Application
import ba.grbo.weatherchecker.WeatherCheckerApplication
import ba.grbo.weatherchecker.util.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkManagerProvider {
    @Singleton
    @Provides
    fun provideNetworkManager(
        application: Application,
        @IODispatcher ioDispatcher: CoroutineDispatcher
    ): NetworkManager {
        val scope = (application as WeatherCheckerApplication).scope
        return NetworkManager(application, ioDispatcher, scope)
    }
}