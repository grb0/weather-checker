package ba.grbo.weatherchecker.data.models.remote.openweather

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