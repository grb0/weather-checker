package ba.grbo.weatherchecker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import java.util.*

@InstallIn(ActivityRetainedComponent::class)
@Module
object LocaleProvider {
    @ActivityRetainedScoped
    @Provides
    fun provideSystemLocale(): Locale = Locale.getDefault()
}