package ba.grbo.weatherchecker.data.source.remote.openweather.adapters

import ba.grbo.weatherchecker.data.models.remote.openweather.Temp
import ba.grbo.weatherchecker.data.source.remote.openweather.annotations.TempMinAndMax
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

@Suppress("UNUSED")
object TempMinAndMaxAdapter {
    private const val MIN = "min"
    private const val MAX = "max"

    @FromJson
    @TempMinAndMax
    fun fromJson(temp: Map<String, Any>) = Temp(temp[MIN] as Double, temp[MAX] as Double)

    @ToJson
    fun toJson(@TempMinAndMax temp: Temp): Map<String, Any> {
        return mapOf(MIN to temp.min, MAX to temp.max)
    }
}