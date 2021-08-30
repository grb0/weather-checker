package ba.grbo.weatherchecker.util

import java.util.*

object Constants {
    const val EMPTY_STRING = ""
    const val SEARCHER_DEBOUNCE_PERIOD = 500L // Can make 2 suggestions network requests per second
    const val COMMON_TAG = "ba.grbo"
    const val EXCEPTIONS_COLLECTION = "exceptions"
    val BOSNIAN_LOCALE = Locale("bs", "BA")
}