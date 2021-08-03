package ba.grbo.weatherchecker.data.models.remote.openweather

data class Forecast(
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>
)