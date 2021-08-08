package ba.grbo.weatherchecker.data.source

import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.data.source.Result.Loading
import ba.grbo.weatherchecker.data.source.Result.SourceResult
import ba.grbo.weatherchecker.data.source.Result.SourceResult.Error
import ba.grbo.weatherchecker.data.source.Result.SourceResult.Success
import ba.grbo.weatherchecker.di.IODispatcher
import ba.grbo.weatherchecker.util.toCoordinates
import ba.grbo.weatherchecker.util.toPlaces
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class DefaultRepository @Inject constructor(
    private val localDataSource: LocalSource,
    private val remoteDataSource: RemoteSource,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : Repository {
    private val updatedError = Error(Exception("Data not updated successfully"))

    private val _suggestedPlaces = MutableStateFlow<Result<List<Place>>?>(null)
    override val suggestedPlaces: StateFlow<Result<List<Place>>?>
        get() = _suggestedPlaces

    private val _overviewedPlaces = MutableStateFlow<Result<List<Place>>?>(null)
    override val overviewedPlaces: StateFlow<Result<List<Place>>?>
        get() = _overviewedPlaces

    override suspend fun updateSuggestedPlaces(location: String, hasInternet: Boolean?) {
        setSuggestedPlacesToLoadingState()
        _suggestedPlaces.value = if (hasInternet == true) {
            getSuggestedPlacesFromNetwork(location)
        } else getSuggestedPlacesFromDatabase(location)
    }

    override suspend fun addOverviewedPlace(
        place: Place,
        overviewedPlacesSize: Int,
        hasInternet: Boolean
    ) {
        _overviewedPlaces.value = Loading
        _overviewedPlaces.value = if (hasInternet) getOverviewedPlacesFromNetwork(
            place,
            overviewedPlacesSize
        ) else getOverviewedPlacesFromDatabase(place, overviewedPlacesSize)
    }

    override suspend fun removeOverviewedPlace(place: Place, onSuccess: () -> Unit) {
        val updatedPlace = place.copy(overviewed = false, overviewedPosition = null)
        _overviewedPlaces.value =
            onSourceResultArrived(localDataSource.updatePlace(updatedPlace)) { updated ->
                if (updated.data) updatedOverviewedPlacesPositionsAndGetUpdatedOverviewedPlaces(
                    onSuccess
                )
                else updatedError
            }
    }

    override suspend fun addOverviewedPlace(place: Place, onSuccess: () -> Unit) {
        _overviewedPlaces.value =
            onSourceResultArrived(localDataSource.getOverviewedPlaces()) { places ->
                val updatedPlaces = places.data.map { overviewedPlace ->
                    if (overviewedPlace.overviewedPosition!! >= place.overviewedPosition!!) {
                        overviewedPlace.copy(overviewedPosition = overviewedPlace.overviewedPosition + 1)
                    } else overviewedPlace
                }
                onSourceResultArrived(localDataSource.updatePlaces(updatedPlaces)) { updated ->
                    if (updated.data) onSourceResultArrived(localDataSource.updatePlace(place)) { updated ->
                        if (updated.data) {
                            onSuccess()
                            getOverviewedPlaces()
                        } else updatedError
                    } else updatedError
                }
            }
    }

    override suspend fun emitOverviewedPlaces(hasInternet: Boolean) {
        _overviewedPlaces.value = Loading
        _overviewedPlaces.value = if (hasInternet) updateAndEmitOverviewedPlaces()
        else emitOverviewedPlaces()
    }

    override suspend fun swapOverviewedPlaces(places: List<Place>, topToBottom: Boolean) {
        val updatedPlaces = if (topToBottom) places.mapIndexed { index, place ->
            if (index == 0) place.copy(overviewedPosition = places[places.lastIndex].overviewedPosition)
            else place.copy(overviewedPosition = place.overviewedPosition?.plus(1))
        } else places.mapIndexed { index, place ->
            if (index == places.lastIndex) {
                place.copy(overviewedPosition = places[0].overviewedPosition)
            } else place.copy(overviewedPosition = place.overviewedPosition?.minus(1))
        }
        _overviewedPlaces.value =
            onSourceResultArrived(localDataSource.updatePlaces(updatedPlaces)) { updated ->
                if (updated.data) getOverviewedPlaces()
                else updatedError
            }
    }

    override suspend fun updateOverviewedPositions() {
        _overviewedPlaces.value = updatedOverviewedPlacesPositionsAndGetUpdatedOverviewedPlaces()
    }

    private suspend fun updatedOverviewedPlacesPositionsAndGetUpdatedOverviewedPlaces(
        onSuccess: (() -> Unit)? = null
    ): SourceResult<List<Place>> {
        return onSourceResultArrived(localDataSource.getOverviewedPlaces()) { places ->
            val updatedPlaces = places.data.mapIndexed { index, place ->
                place.copy(overviewedPosition = places.data.size - index)
            }
            onSourceResultArrived(localDataSource.updatePlaces(updatedPlaces)) { updated ->
                if (updated.data) {
                    onSuccess?.invoke()
                    Success(updatedPlaces)
                } else updatedError
            }
        }
    }

    override fun resetSuggestedPlaces() {
        _suggestedPlaces.value = null
    }

    override fun setSuggestedPlacesToLoadingState() {
        _suggestedPlaces.value = Loading
    }

    private suspend fun updateAndEmitOverviewedPlaces(): SourceResult<List<Place>> {
        return onSourceResultArrived(localDataSource.getOverviewedPlaces()) { places ->
            coroutineScope {
                val deferreds = places.data.map { place ->
                    async(ioDispatcher) { // making requests concurrently
                        val (lat, lon) = place.coordinate
                        onSourceResultArrived(remoteDataSource.getForecast(lat, lon)) { forecast ->
                            val updatedPlace = place.copy(forecast = forecast.data)
                            onSourceResultArrived(localDataSource.updatePlace(updatedPlace)) { updated ->
                                if (updated.data) Success(updatedPlace)
                                else Error(Exception("Data not updated successfully"))
                            }
                        }
                    }
                }
                checkSuccessfulnessAndEmitOverviewedPlaces(deferreds.awaitAll())
            }
        }
    }

    private suspend fun emitOverviewedPlaces(): SourceResult<List<Place>> {
        return onSourceResultArrived(localDataSource.getOverviewedPlaces()) { places -> places }
    }

    private fun checkSuccessfulnessAndEmitOverviewedPlaces(
        places: List<SourceResult<Place>>
    ): SourceResult<List<Place>> {
        return if (places.all { it is Success }) Success(places.map { (it as Success).data })
        else Error((places.first { it is Error } as Error).exception)
    }

    private suspend fun getSuggestedPlacesFromNetwork(location: String): SourceResult<List<Place>> {
        return onSourceResultArrived(remoteDataSource.getSuggestions(location)) { suggestions ->
            val places = suggestions.data.toPlaces()
            onSourceResultArrived(localDataSource.insertPlaces(places)) { inserted ->
                if (inserted.data) onSourceResultArrived(localDataSource.getPlaces(places.toCoordinates())) { places -> places }
                else Error(Exception("Data not inserted successfully"))
            }
        }
    }

    private suspend fun getSuggestedPlacesFromDatabase(location: String): SourceResult<List<Place>> {
        return onSourceResultArrived(localDataSource.getPlaces(location)) { places -> places }
    }

    private suspend fun getOverviewedPlacesFromNetwork(
        place: Place,
        overviewedPlacesSize: Int
    ): SourceResult<List<Place>> {
        val (lat, lon) = place.coordinate
        return onSourceResultArrived(remoteDataSource.getForecast(lat, lon)) { forecast ->
            val updatedPlace = place.copy(
                forecast = forecast.data,
                overviewed = true,
                overviewedPosition = overviewedPlacesSize
            )
            updatePlaceAndGetOverviewedPlacesFromDatabase(updatedPlace)
        }
    }

    private suspend fun getOverviewedPlacesFromDatabase(
        place: Place,
        overviewedPlacesSize: Int
    ): SourceResult<List<Place>> {
        val updatedPlace = place.copy(overviewed = true, overviewedPosition = overviewedPlacesSize)
        return updatePlaceAndGetOverviewedPlacesFromDatabase(updatedPlace)
    }

    private suspend fun updatePlaceAndGetOverviewedPlacesFromDatabase(
        place: Place
    ): SourceResult<List<Place>> {
        return updatePlace(
            place,
            onSuccess = ::getOverviewedPlaces
        )
    }

    private suspend fun getOverviewedPlaces(): SourceResult<List<Place>> {
        return onSourceResultArrived(localDataSource.getOverviewedPlaces()) { places -> places }
    }

    private suspend fun <R> updatePlace(
        place: Place,
        onError: Error = updatedError,
        onSuccess: suspend () -> SourceResult<R>
    ): SourceResult<R> = onSourceResultArrived(localDataSource.updatePlace(place)) { updated ->
        if (updated.data) onSuccess()
        else onError
    }

    private suspend fun <R, T> onSourceResultArrived(
        result: SourceResult<T>,
        onSuccess: suspend (Success<T>) -> SourceResult<R>
    ): SourceResult<R> {
        return when (result) {
            is Error -> result
            is Success -> onSuccess(result)
        }
    }
}