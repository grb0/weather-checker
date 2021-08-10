package ba.grbo.weatherchecker.di

import ba.grbo.weatherchecker.data.source.DefaultRepository
import ba.grbo.weatherchecker.data.source.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
abstract class RepositoryProvider {
    @ActivityRetainedScoped
    @Binds
    abstract fun bindRepository(repository: DefaultRepository): Repository
}