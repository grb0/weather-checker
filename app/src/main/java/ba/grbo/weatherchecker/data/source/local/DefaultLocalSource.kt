package ba.grbo.weatherchecker.data.source.local

import androidx.room.Transaction
import ba.grbo.weatherchecker.data.models.local.Coordinate
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.data.source.LocalSource
import ba.grbo.weatherchecker.data.source.Result.SourceResult
import ba.grbo.weatherchecker.di.IODispatcher
import ba.grbo.weatherchecker.util.toSourceResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.text.Normalizer
import javax.inject.Inject

class DefaultLocalSource @Inject constructor(
    private val placeDao: PlaceDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalSource {
    @Transaction // since we're calling two methods of placeDao (insert and get)
    override suspend fun insertPlaces(places: List<Place>): SourceResult<Boolean> {
        return withContext(ioDispatcher) {
            toSourceResult { arePlacesInsertedSuccessfully(placeDao.insert(places), places) }
        }
    }

    override suspend fun updatePlace(place: Place): SourceResult<Boolean> {
        return withContext(ioDispatcher) {
            toSourceResult { placeDao.update(place) == 1 }
        }
    }

    override suspend fun updatePlaces(places: List<Place>): SourceResult<Boolean> {
        return withContext(ioDispatcher) {
            toSourceResult { placeDao.update(places) == places.size }
        }
    }

    override suspend fun getPlace(coordinate: Coordinate): SourceResult<Place> {
        return withContext(ioDispatcher) {
            toSourceResult { placeDao.get(coordinate) }
        }
    }

    override suspend fun getPlaces(coordinates: List<Coordinate>): SourceResult<List<Place>> {
        return withContext(ioDispatcher) {
            toSourceResult { placeDao.get(coordinates).sortedBy { it.info.countryCode } }
        }
    }

    override suspend fun getPlaces(query: String): SourceResult<List<Place>> {
        return withContext(ioDispatcher) {
            toSourceResult {
                placeDao.get()
                    .filterByQuery(query)
                    .sortedBy { it.info.countryCode }
                    .sortedBy { it.shouldShowDbIcon(false) }
            }
        }
    }

    override fun observePlace(coordinate: Coordinate): Flow<SourceResult<Place>> {
        return placeDao.observe(coordinate).toSourceResult()
    }

    override fun observePlaces(coordinates: List<Coordinate>): Flow<SourceResult<List<Place>>> {
        return placeDao.observe(coordinates)
            .map { places -> places.sortedBy { place -> place.info.countryCode } }
            .flowOn(ioDispatcher)
            .toSourceResult()
    }

    private suspend fun arePlacesInsertedSuccessfully(
        rows: List<Long>,
        places: List<Place>
    ): Boolean {
        val allInserted = rows.size == places.size && rows.all { it != (-1).toLong() }
        return allInserted || arePlacesInDatabase(places)
    }

    // If some rows are -1 we need to make sure those places are actually in the database
    private suspend fun arePlacesInDatabase(places: List<Place>): Boolean {
        val dbPlaces = placeDao.get(places.map(Place::coordinate))
        val placesMap = mutableMapOf<Coordinate, Boolean>()

        dbPlaces.forEach { placesMap[it.coordinate] = true }
        places.forEach { if (it.coordinate !in placesMap) return false }

        return true
    }

    private fun List<Place>.filterByQuery(query: String): List<Place> {
        val normalizedMap = mutableMapOf<String, Map<String, Int>>()
        val kept = mutableSetOf<String>()

        forEach { normalizedMap[it.info.name] = it.info.name.normalize().split() }

        query.lowercase().normalize().split(' ').forEach { word ->
            normalizedMap.run {
                forEach { (name, splittedNames) ->
                    this[name] = splittedNames.toMutableMap().rate(word)
                }

                filter { it.value.hasRatioOver70() }.forEach { (name, _) -> kept.add(name) }
            }
        }

        return filter { it.info.name in kept }
    }

    private fun Map<String, Int>.hasRatioOver70(): Boolean {
        forEach { (_, ratio) -> if (ratio > 70) return true }
        return false
    }

    private fun MutableMap<String, Int>.rate(query: String): Map<String, Int> {
        forEach { (word, _) -> this[word] = FuzzySearch.weightedRatio(query, word) }
        return this
    }

    private fun String.split(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        split(", ").forEach { word ->
            result[word] = -1
        }
        return result
    }

    private fun String.normalize(): String {
        return Normalizer
            .normalize(this, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]".toRegex(), "")
    }
}