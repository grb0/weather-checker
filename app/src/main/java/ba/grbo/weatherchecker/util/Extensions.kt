package ba.grbo.weatherchecker.util

import android.content.res.Resources
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.data.models.remote.locationiq.Suggestion
import ba.grbo.weatherchecker.data.source.Result.SourceResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Note: Make sure to collect from flow before any value is emitted, otherwise all values emitted
// before collecting the flow are lost (not acknowledged).
@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T> SingleSharedFlow() = MutableSharedFlow<T>(
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    extraBufferCapacity = 1
)

fun Long.toFormattedDate(locale: Locale): String = SimpleDateFormat(
    DateFormat.getBestDateTimePattern(locale, "EEEE MMMM d"),
    locale
).format(Date(this))

fun Long.toFormattedTime(locale: Locale): String = SimpleDateFormat.getTimeInstance(
    SimpleDateFormat.SHORT,
    locale
).format(Date(this))

fun Float.toPixels(resources: Resources) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    resources.displayMetrics
)

fun List<Suggestion>.toPlaces() = map(Suggestion::toPlace)

fun View.setCustomTopMargin(margin: Float) {
    val layoutParams = layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.run {
        topMargin = margin.roundToInt()
    }
    this.layoutParams = layoutParams
}

fun RecyclerView.addDivider(@DrawableRes drawableRes: Int) {
    val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
        setDrawable(ContextCompat.getDrawable(context, drawableRes)!!)
    }
    addItemDecoration(divider)
}

suspend fun <T> toSourceResult(input: suspend () -> T): SourceResult<T> = try {
    SourceResult.Success(input())
} catch (e: Exception) {
    SourceResult.Error(e)
}

fun <R> Flow<R>.toSourceResult(): Flow<SourceResult<R>> = map {
    try {
        SourceResult.Success(it)
    } catch (e: Exception) {
        SourceResult.Error(e)
    }
}

fun List<Place>.toCoordinates() = map(Place::coordinate)