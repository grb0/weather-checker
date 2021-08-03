package ba.grbo.weatherchecker.di

import ba.grbo.weatherchecker.data.source.DefaultRepository
import ba.grbo.weatherchecker.data.source.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@InstallIn(ActivityRetainedComponent::class)
@Module
abstract class RepositoryProvider {
    @Binds
    abstract fun bindRepository(repository: DefaultRepository): Repository
}