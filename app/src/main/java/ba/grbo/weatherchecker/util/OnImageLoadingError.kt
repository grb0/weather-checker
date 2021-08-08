package ba.grbo.weatherchecker.util

fun interface OnImageLoadingError {
    fun onError(throwable: Throwable)
}