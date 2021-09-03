package ba.grbo.weatherchecker.di

import android.app.Application
import ba.grbo.weatherchecker.util.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

@InstallIn(ActivityRetainedComponent::class)
@Module
object NetworkManagerProvider {
    @ActivityRetainedScoped
    @Provides
    fun provideCoroutineScope(component: ActivityRetainedLifecycle): CoroutineScope {
        val scope = MainScope()
        component.addOnClearedListener { scope.cancel() }
        return scope
    }

    @ActivityRetainedScoped
    @Provides
    fun provideNetworkManager(
        application: Application,
        scope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher
    ): NetworkManager {
        return NetworkManager(application, ioDispatcher, scope)
    }
}