package ba.grbo.weatherchecker.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ba.grbo.weatherchecker.data.source.network.NetworkWeather

@Entity(tableName = "places_table")
data class Place(
    @PrimaryKey
    val coordinate: Coordinate,
    val info: Info,
    val weather: NetworkWeather? = null
)

data class Coordinate(
    val latitude: String,
    val longitude: String
)

data class Info(
    val place: String,
    val name: String,
    val countryCodeResource: Int
)