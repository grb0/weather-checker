package ba.grbo.weatherchecker.data.source.remote.locationiq

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

@Suppress("UNUSED")
object AddressAdapter {
    private const val COUNTRY_CODE = "country_code"

    @FromJson
    @AddressCountryCode
    fun fromJson(address: Map<String?, Any?>): String? {
        return if (address.containsKey(COUNTRY_CODE)) address[COUNTRY_CODE] as String else null
    }

    @ToJson
    fun toJson(@AddressCountryCode countryCode: String?): Map<String?, Any?> {
        return if (countryCode != null) mapOf(COUNTRY_CODE to countryCode)
        else mapOf()
    }
}