package ba.grbo.weatherchecker.data.source.local

import androidx.room.*

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
}