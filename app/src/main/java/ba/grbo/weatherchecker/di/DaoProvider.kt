package ba.grbo.weatherchecker.di

import ba.grbo.weatherchecker.data.source.local.WeatherCheckerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@InstallIn(ActivityRetainedComponent::class)
@Module
object DaoProvider {
    @Provides
    fun providePlaceDao(database: WeatherCheckerDatabase) = database.placeDao
}