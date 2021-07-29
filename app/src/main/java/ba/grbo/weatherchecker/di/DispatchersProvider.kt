package ba.grbo.weatherchecker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers

@InstallIn(ActivityRetainedComponent::class)
@Module
object DispatchersProvider {
    @ActivityRetainedScoped
    @IODispatcher
    @Provides
    fun provideIODispatcher() = Dispatchers.IO

    @ActivityRetainedScoped
    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher() = Dispatchers.Default
}