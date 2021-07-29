package ba.grbo.weatherchecker.util

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.Resources
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.FragmentContainerView
import ba.grbo.weatherchecker.R
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BannerAnimator(
    private val resources: Resources,
    private val doOnEnd: DoOnEnd,
    banner: TextView,
    fragment: FragmentContainerView,
) {
    private val translationLength = banner.height + 16f.toPixels(resources)
    private val bannerAnimator = getTranslateObjectAnimator(banner, translationLength)
    private val fragmentAnimator = getShrinkObjectAnimator(
        fragment,
        fragment.height.toFloat(),
        fragment.height - translationLength
    )

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
        launch { fragmentAnimator.begin() }
    }

    private suspend fun reverse() = coroutineScope {
        launch { bannerAnimator.reverse() }
        launch { fragmentAnimator.reverse() }
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

    private fun getTranslateObjectAnimator(
        view: TextView,
        translationLength: Float
    ) = ObjectAnimator.ofFloat(
        view,
        View.TRANSLATION_Y,
        view.translationY,
        translationLength
    ).setUp(resources)

    private fun getShrinkObjectAnimator(
        view: FragmentContainerView,
        startHeight: Float,
        endHeight: Float
    ): ObjectAnimator {
        val a = PropertyValuesHolder.ofObject(
            "height",
            { fraction, _, _ ->
                val height = startHeight + (fraction * (endHeight - startHeight))
                val lP = view.layoutParams
                lP.height = height.roundToInt()
                view.layoutParams = lP
                height
            },
            startHeight,
            endHeight
        )

        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            a
        ).setUp(resources)
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