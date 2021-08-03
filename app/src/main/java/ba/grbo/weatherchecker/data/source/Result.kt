package ba.grbo.weatherchecker.data.source



sealed class Result<out R> {
    sealed class SourceResult<out R>: Result<R>() {
        data class Success<out T>(val data: T) : SourceResult<T>()
        data class Error(val exception: Exception) : SourceResult<Nothing>()
    }

    object Loading : Result<Nothing>()

    override fun toString() = when (this) {
        is SourceResult.Success<*> -> "Success[data=$data]"
        is SourceResult.Error -> "Error[exception=$exception]"
        is Loading -> "Loading"
    }
}
