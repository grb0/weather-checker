package ba.grbo.weatherchecker.data.source.local

import androidx.room.*
import ba.grbo.weatherchecker.data.models.local.Coordinate
import ba.grbo.weatherchecker.data.models.local.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(places: List<Place>): List<Long>

    @Update
    suspend fun update(place: Place): Int

    @Update
    suspend fun update(places: List<Place>): Int

    @Query("SELECT * FROM places_table WHERE coordinate = :coordinate")
    suspend fun get(coordinate: Coordinate): Place

    @Query("SELECT * FROM places_table WHERE coordinate IN (:coordinates)")
    suspend fun get(coordinates: List<Coordinate>): List<Place>

    @Query("SELECT * FROM places_table")
    suspend fun get(): List<Place>

    @Query("SELECT * FROM places_table WHERE overviewed = 1 ORDER BY overviewedPosition DESC")
    suspend fun getOverviewed(): List<Place>

    @Query("SELECT * FROM places_table WHERE coordinate = :coordinate")
    fun observe(coordinate: Coordinate): Flow<Place>

    @Query("SELECT * FROM places_table WHERE overviewed = 1")
    fun observe(): Flow<List<Place>>
}