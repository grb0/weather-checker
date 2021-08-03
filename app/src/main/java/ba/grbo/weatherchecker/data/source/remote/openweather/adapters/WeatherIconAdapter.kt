package ba.grbo.weatherchecker.data.source.remote.openweather.adapters

import ba.grbo.weatherchecker.data.source.remote.openweather.annotations.WeatherIcon
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

@Suppress("UNUSED")
object WeatherIconAdapter {
    private const val ICON = "icon"

    @FromJson
    @WeatherIcon
    fun fromJson(weather: List<Map<String, Any>>) = weather[0][ICON] as String

    @ToJson
    fun toJson(@WeatherIcon icon: String): List<Map<String, Any>> {
        return listOf(mapOf(ICON to icon))
    }
}