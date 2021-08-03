package ba.grbo.weatherchecker.data.models.remote.openweather

import ba.grbo.weatherchecker.data.source.remote.openweather.annotations.WeatherDescriptionAndIcon
import com.squareup.moshi.Json

data class CurrentWeather(
    @Json(name = "dt")
    override val timestamp: Double,
    val temp: Double,
    @Json(name = "feels_like")
    val feelsLike: Double,
    override val pressure: Double,
    override val humidity: Double,
    @Json(name = "dew_point")
    override val dewPoint: Double,
    override val uvi: Double,
    val visibility: Double,
    @Json(name = "wind_speed")
    override val windSpeed: Double,
    @Json(name = "wind_deg")
    override val windDegrees: Double,
    @WeatherDescriptionAndIcon
    @Json(name = "weather")
    override val weather: Weather,
) : CommonWeather