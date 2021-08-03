package ba.grbo.weatherchecker.data.models.remote.openweather

import ba.grbo.weatherchecker.data.source.remote.openweather.annotations.WeatherIcon
import com.squareup.moshi.Json

data class HourlyWeather(
    @Json(name = "dt")
    val timestamp: Double,
    val temp: Double,
    @WeatherIcon
    @Json(name = "weather")
    val icon: String
)