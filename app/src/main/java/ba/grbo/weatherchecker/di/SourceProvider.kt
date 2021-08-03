package ba.grbo.weatherchecker.di

import ba.grbo.weatherchecker.data.source.LocalSource
import ba.grbo.weatherchecker.data.source.RemoteSource
import ba.grbo.weatherchecker.data.source.local.DefaultLocalSource
import ba.grbo.weatherchecker.data.source.remote.DefaultRemoteSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
abstract class SourceProvider {
    @ActivityRetainedScoped
    @Binds
    abstract fun bindLocalSource(source: DefaultLocalSource): LocalSource

    @ActivityRetainedScoped
    @Binds
    abstract fun bindRemoteSource(source: DefaultRemoteSource): RemoteSource
}