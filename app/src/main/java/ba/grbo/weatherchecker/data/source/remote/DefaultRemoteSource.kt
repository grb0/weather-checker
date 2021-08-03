package ba.grbo.weatherchecker.data.source.remote

import ba.grbo.weatherchecker.data.models.remote.locationiq.Suggestion
import ba.grbo.weatherchecker.data.models.remote.openweather.Forecast
import ba.grbo.weatherchecker.data.source.RemoteSource
import ba.grbo.weatherchecker.data.source.Result.SourceResult
import ba.grbo.weatherchecker.data.source.remote.locationiq.LocationIQService
import ba.grbo.weatherchecker.data.source.remote.openweather.OpenWeatherService
import ba.grbo.weatherchecker.di.IODispatcher
import ba.grbo.weatherchecker.util.toSourceResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultRemoteSource @Inject constructor(
    private val locationIQService: LocationIQService,
    private val openWeatherService: OpenWeatherService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSource {
    override suspend fun getSuggestions(location: String): SourceResult<List<Suggestion>> {
        return withContext(ioDispatcher) {
            toSourceResult { locationIQService.getSuggestions(location) }
        }
    }

    override suspend fun getForecast(latitude: String, longitude: String): SourceResult<Forecast> {
        return withContext(ioDispatcher) {
            toSourceResult { openWeatherService.getForecast(latitude, longitude) }
        }
    }
}