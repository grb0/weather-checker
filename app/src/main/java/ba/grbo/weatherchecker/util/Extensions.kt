package ba.grbo.weatherchecker.util

import android.content.res.Resources
import android.text.format.DateFormat
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import ba.grbo.weatherchecker.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.text.SimpleDateFormat
import java.util.*

fun AlphaAnimation.setUp(resources: Resources): AlphaAnimation {
    interpolator = LinearInterpolator()
    duration = resources.getInteger(R.integer.anim_time).toLong()
    return this
}


// Note: Make sure to collect from flow before any value is emitted, otherwise all values emitted
// before collecting the flow are lost (not acknowledged).
@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T> SingleSharedFlow() = MutableSharedFlow<T>(
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    extraBufferCapacity = 1
)

fun Long.toFormattedDate(locale: Locale):String = SimpleDateFormat(
    DateFormat.getBestDateTimePattern(locale, "EEEE MMMM d"),
    locale
).format(Date(this))

fun Long.toFormattedTime(locale: Locale): String = SimpleDateFormat.getTimeInstance(
    SimpleDateFormat.SHORT,
    locale
).format(Date(this))