package ba.grbo.weatherchecker.data.source

import ba.grbo.weatherchecker.data.models.remote.locationiq.Suggestion
import ba.grbo.weatherchecker.data.models.remote.openweather.Forecast
import ba.grbo.weatherchecker.data.source.Result.SourceResult

interface RemoteSource {
    suspend fun getSuggestions(location: String): SourceResult<List<Suggestion>>

    suspend fun getForecast(latitude: String, longitude: String): SourceResult<Forecast>
}