package ba.grbo.weatherchecker.di

import android.content.Context
import ba.grbo.weatherchecker.data.source.local.WeatherCheckerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
object DatabaseProvider {
    @ActivityRetainedScoped
    @Provides
    fun provideWeatherCheckerDatabase(
        @ApplicationContext context: Context
    ): WeatherCheckerDatabase = WeatherCheckerDatabase.getInstance(context)
}