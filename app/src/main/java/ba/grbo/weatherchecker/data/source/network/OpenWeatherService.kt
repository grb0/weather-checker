package ba.grbo.weatherchecker.data.source.network

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
    companion object {
        const val API_KEY = "6b7eec8f84e40ebc73231a8a92b8f187"
    }

    @GET("data/2.5/onecall?appid=$API_KEY&&units=metric&exclude=minutely")
    suspend fun getWeather(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String
    ): NetworkWeather
}

data class NetworkWeather(
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>
)

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

data class Weather(
    val description: String,
    val icon: String
)

data class Temp(
    val min: Double,
    val max: Double
)

data class HourlyWeather(
    @Json(name = "dt")
    val timestamp: Double,
    val temp: Double,
    @WeatherIcon
    @Json(name = "weather")
    val icon: String
)

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class WeatherDescriptionAndIcon

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class WeatherIcon

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class TempMinAndMax

@Suppress("UNUSED")
object WeatherDescriptionAndIconAdapter {
    @FromJson
    @WeatherDescriptionAndIcon
    fun fromJson(weather: List<Map<String, Any>>) = Weather(
        weather[0]["description"] as String,
        weather[0]["icon"] as String
    )

    @ToJson
    fun toJson(@WeatherDescriptionAndIcon weather: Weather): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "description" to weather.description,
                "icon" to weather.icon
            )
        )
    }
}

@Suppress("UNUSED")
object WeatherIconAdapter {
    @FromJson
    @WeatherIcon
    fun fromJson(weather: List<Map<String, Any>>) = weather[0]["icon"] as String

    @ToJson
    fun toJson(@WeatherIcon icon: String): List<Map<String, Any>> {
        return listOf(mapOf("icon" to icon))
    }
}

@Suppress("UNUSED")
object TempMinAndMaxAdapter {
    @FromJson
    @TempMinAndMax
    fun fromJson(temp: Map<String, Any>) = Temp(temp["min"] as Double, temp["max"] as Double)

    @ToJson
    fun toJson(@TempMinAndMax temp: Temp): Map<String, Any> {
        return mapOf("min" to temp.min, "max" to temp.max)
    }
}

fun createOpenWeatherService(): OpenWeatherService {
    val baseUrl = "https://api.openweathermap.org"

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(WeatherDescriptionAndIconAdapter)
        .add(WeatherIconAdapter)
        .add(TempMinAndMaxAdapter)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    return retrofit.create(OpenWeatherService::class.java)
}

interface CommonWeather {
    val timestamp: Double
    val pressure: Double
    val humidity: Double
    val dewPoint: Double
    val uvi: Double
    val windSpeed: Double
    val windDegrees: Double
    val weather: Weather
}