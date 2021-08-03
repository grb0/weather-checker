package ba.grbo.weatherchecker.data.models.local

data class Info(
    val place: String,
    val name: String,
    val countryCode: String?,
    val countryCodeResource: Int
)