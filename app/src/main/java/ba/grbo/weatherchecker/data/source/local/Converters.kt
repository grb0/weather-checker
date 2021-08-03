package ba.grbo.weatherchecker.data.source.local

import androidx.room.TypeConverter
import ba.grbo.weatherchecker.data.models.local.Coordinate
import ba.grbo.weatherchecker.data.models.local.Info
import ba.grbo.weatherchecker.data.models.remote.openweather.Forecast
import ba.grbo.weatherchecker.data.source.remote.openweather.adapters.TempMinAndMaxAdapter
import ba.grbo.weatherchecker.data.source.remote.openweather.adapters.WeatherDescriptionAndIconAdapter
import ba.grbo.weatherchecker.data.source.remote.openweather.adapters.WeatherIconAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(WeatherDescriptionAndIconAdapter)
            .add(WeatherIconAdapter)
            .add(TempMinAndMaxAdapter)
            .build()
    }

    private val forecastAdapter by lazy { moshi.adapter(Forecast::class.java) }
    private val coordinateAdapter by lazy { moshi.adapter(Coordinate::class.java) }
    private val infoAdapter by lazy { moshi.adapter(Info::class.java) }

    @TypeConverter
    fun coordinateToJson(coordinate: Coordinate): String = coordinateAdapter.toJson(coordinate)

    @TypeConverter
    fun jsonToCoordinate(json: String) = coordinateAdapter.fromJson(json)!!

    @TypeConverter
    fun infoToJson(info: Info): String = infoAdapter.toJson(info)

    @TypeConverter
    fun jsonToInfo(json: String) = infoAdapter.fromJson(json)

    @TypeConverter
    fun forecastToJson(forecast: Forecast?): String = forecastAdapter.toJson(forecast)

    @TypeConverter
    fun jsonToForecast(json: String) = forecastAdapter.fromJson(json)
}