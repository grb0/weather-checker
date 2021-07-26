package ba.grbo.weatherchecker.util

import android.content.res.Resources
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import ba.grbo.weatherchecker.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

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