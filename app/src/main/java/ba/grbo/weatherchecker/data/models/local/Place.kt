package ba.grbo.weatherchecker.data.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ba.grbo.weatherchecker.data.models.remote.openweather.Forecast

@Entity(tableName = "places_table")
data class Place(
    @PrimaryKey
    val coordinate: Coordinate,
    val info: Info,
    val forecast: Forecast? = null
) {
    fun shouldShowDbIcon(hasInternet: Boolean) = !hasInternet && forecast != null
}
