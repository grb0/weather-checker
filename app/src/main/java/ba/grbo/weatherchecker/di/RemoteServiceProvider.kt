package ba.grbo.weatherchecker.di

import ba.grbo.weatherchecker.data.source.remote.locationiq.AddressAdapter
import ba.grbo.weatherchecker.data.source.remote.locationiq.LocationIQService
import ba.grbo.weatherchecker.data.source.remote.openweather.OpenWeatherService
import ba.grbo.weatherchecker.data.source.remote.openweather.adapters.TempMinAndMaxAdapter
import ba.grbo.weatherchecker.data.source.remote.openweather.adapters.WeatherDescriptionAndIconAdapter
import ba.grbo.weatherchecker.data.source.remote.openweather.adapters.WeatherIconAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


@InstallIn(ActivityRetainedComponent::class)
@Module
object RemoteServiceProvider {
    @ActivityRetainedScoped
    @Provides
    fun provideLocationIQService(
        client: OkHttpClient
    ): LocationIQService = synchronized(this) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(AddressAdapter)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(LocationIQService.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        retrofit.create(LocationIQService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideOpenWeatherService(
        client: OkHttpClient
    ): OpenWeatherService = synchronized(this) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(WeatherDescriptionAndIconAdapter)
            .add(WeatherIconAdapter)
            .add(TempMinAndMaxAdapter)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(OpenWeatherService.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        retrofit.create(OpenWeatherService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))
        .protocols(listOf(Protocol.HTTP_1_1))
        .build()
}