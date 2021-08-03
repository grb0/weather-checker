package ba.grbo.weatherchecker.data.models.remote.locationiq

import ba.grbo.weatherchecker.data.models.local.Coordinate
import ba.grbo.weatherchecker.data.models.local.Info
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.data.source.remote.locationiq.AddressCountryCode
import ba.grbo.weatherchecker.util.Country
import com.squareup.moshi.Json

data class Suggestion(
    @Json(name = "display_name")
    val name: String,
    val place: String = name.substringBefore(','),
    @Json(name = "lat")
    val latitude: String,
    @Json(name = "lon")
    val longitude: String,
    @AddressCountryCode
    @Json(name = "address")
    private val countryCode: String?
) {
   private val countryFlagResource = Country.toFlagResource(countryCode)

    fun toPlace() = Place(
        Coordinate(latitude, longitude),
        Info(place, name, countryCode, countryFlagResource)
    )
}