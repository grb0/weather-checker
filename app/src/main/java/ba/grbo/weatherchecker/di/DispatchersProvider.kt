package ba.grbo.weatherchecker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DispatchersProvider {
    @Singleton
    @MainDispatcher
    @Provides
    fun provideMainDispatcher() = Dispatchers.Main

    @Singleton
    @IODispatcher
    @Provides
    fun provideIODispatcher() = Dispatchers.IO

    @Singleton
    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher() = Dispatchers.Default
}