package ba.grbo.weatherchecker.data.source.remote.locationiq

import ba.grbo.weatherchecker.BuildConfig
import ba.grbo.weatherchecker.data.models.remote.locationiq.Suggestion
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationIQService {
    companion object {
        const val BASE_URL = "https://api.locationiq.com"
        private const val QUERY = "q" // defined by the service
    }

    @GET("v1/autocomplete.php?key=${BuildConfig.LOCATION_IQ_API_KEY}&limit=20&dedupe=1")
    suspend fun getSuggestions(@Query(QUERY) query: String): List<Suggestion>
}