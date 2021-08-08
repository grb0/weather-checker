package ba.grbo.weatherchecker.data.source

import ba.grbo.weatherchecker.data.models.local.Place
import kotlinx.coroutines.flow.StateFlow

interface Repository {
    val suggestedPlaces: StateFlow<Result<List<Place>>?>
    val overviewedPlaces: StateFlow<Result<List<Place>>?>

    suspend fun updateSuggestedPlaces(location: String, hasInternet: Boolean? = null)

    suspend fun addOverviewedPlace(
        place: Place,
        overviewedPlacesSize: Int,
        hasInternet: Boolean
    )

    suspend fun removeOverviewedPlace(place: Place, onSuccess: () -> Unit)

    suspend fun addOverviewedPlace(place: Place, onSuccess: () -> Unit)

    suspend fun emitOverviewedPlaces(hasInternet: Boolean)

    suspend fun swapOverviewedPlaces(places: List<Place>, topToBottom: Boolean)

    suspend fun updateOverviewedPositions()

    fun resetSuggestedPlaces()

    fun setSuggestedPlacesToLoadingState()
}