package ba.grbo.weatherchecker.util

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.Resources
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.animation.doOnEnd
import ba.grbo.weatherchecker.R
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class BannerAnimator(
    private val banner: TextView,
    private val doOnEnd: DoOnEnd,
) {
    private val bannerAnimator = getBannerAnimator(banner)
    private val blinkAnimation = AlphaAnimation(1f, 0f).apply {
        duration = 50
        repeatMode = Animation.REVERSE
        repeatCount = 3
        startOffset = 20
    }

    // 15.999 and 34.001 instead of 16 and 34, because for whatever reason when banner is dismissed
    // a small portion of its bottom border stays visible
    private fun getBannerAnimator(
        view: TextView,
        startHeight: Float = (-34.001f).toPixels(view.resources),
        endHeight: Float = 15.999f.toPixels(view.resources)
    ): ObjectAnimator {
        val layoutParams = PropertyValuesHolder.ofObject(
            "layoutParams",
            { fraction, _, _ ->
                val height = startHeight + (fraction * (endHeight - startHeight))
                view.setCustomTopMargin(height)
                height
            },
            startHeight,
            endHeight
        )

        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            layoutParams
        ).setUp(view.resources)
    }

    fun blink() {
        banner.startAnimation(blinkAnimation)
    }

    suspend fun onAnimating() {
        doOnEnd(ANIMATING)
        start()
    }

    suspend fun onAnimatingInterrupted() {
        doOnEnd(ANIMATING_INTERRUPTED)
        start()
    }

    suspend fun onReverseAnimating() {
        doOnEnd(REVERSE_ANIMATING)
        reverse()
    }

    suspend fun onReverseAnimatingInterrupted() {
        doOnEnd(REVERSE_ANIMATING_INTERRUPTED)
        reverse()
    }

    private suspend fun start() = coroutineScope {
        launch { bannerAnimator.begin() }
    }

    private suspend fun reverse() = coroutineScope {
        launch { bannerAnimator.reverse() }
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun doOnEnd(animationState: AnimationState) {
        bannerAnimator.removeAllListeners()
        bannerAnimator.doOnEnd {
            when (animationState) {
                ANIMATING -> doOnEnd.onAnimated()
                REVERSE_ANIMATING -> doOnEnd.onReverseAnimated()
                ANIMATING_INTERRUPTED -> doOnEnd.onAnimatingInterrupted()
                REVERSE_ANIMATING_INTERRUPTED -> doOnEnd.onReverseAnimatingInterrupted()
            }
        }
    }

    private fun ObjectAnimator.setUp(resources: Resources): ObjectAnimator {
        interpolator = AccelerateDecelerateInterpolator()
        duration = resources.getInteger(R.integer.anim_time).toLong()
        return this
    }

    private fun ObjectAnimator.begin() {
        if (isRunning) reverse()
        else start()
    }

    data class DoOnEnd(
        val onAnimated: () -> Unit,
        val onReverseAnimated: () -> Unit,
        val onAnimatingInterrupted: () -> Unit,
        val onReverseAnimatingInterrupted: () -> Unit
    )
}