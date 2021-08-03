package ba.grbo.weatherchecker.data.source

import ba.grbo.weatherchecker.data.models.local.Coordinate
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.data.source.Result.SourceResult
import kotlinx.coroutines.flow.Flow

interface LocalSource {
    suspend fun insertPlaces(places: List<Place>): SourceResult<Boolean>

    suspend fun updatePlace(place: Place): SourceResult<Boolean>

    suspend fun updatePlaces(places: List<Place>): SourceResult<Boolean>

    suspend fun getPlace(coordinate: Coordinate): SourceResult<Place>

    suspend fun getPlaces(coordinates: List<Coordinate>): SourceResult<List<Place>>

    suspend fun getPlaces(query: String): SourceResult<List<Place>>

    fun observePlace(coordinate: Coordinate): Flow<SourceResult<Place>>

    fun observePlaces(coordinates: List<Coordinate>): Flow<SourceResult<List<Place>>>
}