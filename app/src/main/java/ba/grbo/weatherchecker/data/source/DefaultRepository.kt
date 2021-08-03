package ba.grbo.weatherchecker.data.source

import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.data.source.Result.Loading
import ba.grbo.weatherchecker.data.source.Result.SourceResult
import ba.grbo.weatherchecker.data.source.Result.SourceResult.Error
import ba.grbo.weatherchecker.data.source.Result.SourceResult.Success
import ba.grbo.weatherchecker.util.toCoordinates
import ba.grbo.weatherchecker.util.toPlaces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class DefaultRepository @Inject constructor(
    private val localDataSource: LocalSource,
    private val remoteDataSource: RemoteSource
) : Repository {
    private val _suggestedPlaces = MutableStateFlow<Result<List<Place>>?>(null)
    override val suggestedPlaces: StateFlow<Result<List<Place>>?>
        get() = _suggestedPlaces

    override suspend fun updateSuggestedPlaces(location: String?, hasInternet: Boolean?) {
        if (location == null) _suggestedPlaces.value = null
        else {
            _suggestedPlaces.value = Loading
            _suggestedPlaces.value = if (hasInternet == true) onSourceResultArrived(
                remoteDataSource.getSuggestions(location)
            ) { suggestions ->
                val places = suggestions.data.toPlaces()
                onSourceResultArrived(localDataSource.insertPlaces(places)) {
                    localDataSource.getPlaces(places.toCoordinates())
                }
            } else onSourceResultArrived(localDataSource.getPlaces(location)) { places -> places }
        }
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