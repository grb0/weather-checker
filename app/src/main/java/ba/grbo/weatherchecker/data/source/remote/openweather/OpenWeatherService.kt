package ba.grbo.weatherchecker.data.source.remote.openweather

import ba.grbo.weatherchecker.data.models.remote.openweather.Forecast
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
    companion object {
        const val BASE_URL = "https://api.openweathermap.org"

        private const val API_KEY = "6b7eec8f84e40ebc73231a8a92b8f187"
        private const val LATITUDE = "lat" // defined by the sevice
        private const val LONGITUDE = "lon" // defined by the sevice
    }

    @GET("data/2.5/onecall?appid=$API_KEY&&units=metric&exclude=minutely")
    suspend fun getForecast(
        @Query(LATITUDE) latitude: String,
        @Query(LONGITUDE) longitude: String
    ): Forecast
}