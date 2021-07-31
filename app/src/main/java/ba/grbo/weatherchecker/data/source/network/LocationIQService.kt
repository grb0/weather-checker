package ba.grbo.weatherchecker.data.source.network

import ba.grbo.weatherchecker.data.source.local.Coordinate
import ba.grbo.weatherchecker.data.source.local.Info
import ba.grbo.weatherchecker.data.source.local.Place
import ba.grbo.weatherchecker.util.Country
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationIQService {
    companion object {
        const val API_KEY = "pk.940c49bb96c565b7f683c55497532968"
    }

    @GET("v1/autocomplete.php?key=$API_KEY&limit=20&dedupe=1")
    suspend fun getSuggestions(
        @Query("q") query: String,
    ): List<NetworkSuggestion>
}

data class NetworkSuggestion(
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
        Info(place, name, countryFlagResource)
    )
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class AddressCountryCode

@Suppress("UNUSED")
object AddressAdapter {
    @FromJson
    @AddressCountryCode
    fun fromJson(address: Map<String?, Any?>): String? {
        return if (address.containsKey("country_code")) address["country_code"] as String else null
    }

    @ToJson
    fun toJson(@AddressCountryCode countryCode: String?): Map<String?, Any?> {
        return if (countryCode != null) mapOf("country_code" to countryCode)
        else mapOf()
    }
}

fun createLocationIQService(): LocationIQService {
    val baseUrl = "https://api.locationiq.com"

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(AddressAdapter)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    return retrofit.create(LocationIQService::class.java)
}