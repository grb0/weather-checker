package ba.grbo.weatherchecker.data.source.local

import androidx.room.TypeConverter
import ba.grbo.weatherchecker.data.source.network.NetworkWeather
import ba.grbo.weatherchecker.data.source.network.TempMinAndMaxAdapter
import ba.grbo.weatherchecker.data.source.network.WeatherDescriptionAndIconAdapter
import ba.grbo.weatherchecker.data.source.network.WeatherIconAdapter
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

    private val weatherAdapter by lazy { moshi.adapter(NetworkWeather::class.java) }

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
    fun weatherToJson(weather: NetworkWeather?): String = weatherAdapter.toJson(weather)

    @TypeConverter
    fun jsonToWeather(json: String) = weatherAdapter.fromJson(json) // can be null
}