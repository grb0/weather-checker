package ba.grbo.weatherchecker.data.source

import ba.grbo.weatherchecker.data.models.local.Place
import kotlinx.coroutines.flow.StateFlow

interface Repository {
    val suggestedPlaces: StateFlow<Result<List<Place>>?>

    suspend fun updateSuggestedPlaces(location: String?, hasInternet: Boolean? = null)
}