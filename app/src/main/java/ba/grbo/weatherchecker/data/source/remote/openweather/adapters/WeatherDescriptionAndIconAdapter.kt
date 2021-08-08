package ba.grbo.weatherchecker.data.source.remote.openweather.adapters

import ba.grbo.weatherchecker.data.models.remote.openweather.Weather
import ba.grbo.weatherchecker.data.source.remote.openweather.annotations.WeatherDescriptionAndIcon
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

@Suppress("UNUSED")
object WeatherDescriptionAndIconAdapter {
    private const val DESCRIPTION = "description"
    private const val ICON = "icon"

    @FromJson
    @WeatherDescriptionAndIcon
    fun fromJson(weather: List<Map<String, Any>>) = Weather(
        (weather[0][DESCRIPTION] as String).replaceFirstChar { it.titlecase(Locale.getDefault()) },
        weather[0][ICON] as String
    )

    @ToJson
    fun toJson(@WeatherDescriptionAndIcon weather: Weather): List<Map<String, Any>> {
        return listOf(
            mapOf(
                DESCRIPTION to weather.description,
                ICON to weather.icon
            )
        )
    }
}