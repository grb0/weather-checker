package ba.grbo.weatherchecker.data.models.remote.openweather

import ba.grbo.weatherchecker.data.source.remote.openweather.annotations.TempMinAndMax
import ba.grbo.weatherchecker.data.source.remote.openweather.annotations.WeatherDescriptionAndIcon
import com.squareup.moshi.Json

data class DailyWeather(
    @Json(name = "dt")
    override val timestamp: Double,
    @TempMinAndMax
    val temp: Temp,
    override val pressure: Double,
    override val humidity: Double,
    @Json(name = "dew_point")
    override val dewPoint: Double,
    override val uvi: Double,
    @Json(name = "wind_speed")
    override val windSpeed: Double,
    @Json(name = "wind_deg")
    override val windDegrees: Double,
    @WeatherDescriptionAndIcon
    @Json(name = "weather")
    override val weather: Weather,
) : CommonWeather