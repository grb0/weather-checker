package ba.grbo.weatherchecker.data.models.local

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import ba.grbo.weatherchecker.data.models.remote.openweather.Forecast

@Entity(tableName = "places_table")
data class Place(
    @PrimaryKey
    val coordinate: Coordinate,
    val info: Info,
    val forecast: Forecast? = null,
    val overviewed: Boolean = false, // overviewed or observed ...
    val overviewedPosition: Int? = null // need it here for copy function
) {
    @Ignore
    val cached = forecast != null

    fun shouldShowDbIcon(hasInternet: Boolean) = !hasInternet && forecast != null

    // Need to exclude overviewedPosition from equality if we want smooth drag and drop,
    // since we don't actually need overviewed, it's excluded as well
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Place

        if (coordinate != other.coordinate) return false
        if (info != other.info) return false
        if (forecast != other.forecast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coordinate.hashCode()
        result = 31 * result + info.hashCode()
        result = 31 * result + (forecast?.hashCode() ?: 0)
        return result
    }
}

