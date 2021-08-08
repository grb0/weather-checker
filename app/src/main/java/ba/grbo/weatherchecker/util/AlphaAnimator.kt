package ba.grbo.weatherchecker.util

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation.AnimationListener
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import ba.grbo.weatherchecker.R
import com.google.android.material.card.MaterialCardView

class AlphaAnimator(
    locationResetter: ImageButton,
    suggestedPlaces: RecyclerView,
    suggestedPlacesCard: MaterialCardView,
    overviewedPlacesCard: MaterialCardView,
    emptySuggestedPlacesInfo: ImageView,
    emptyOverviewedPlacesInfo: LinearLayout,
    onSuggestedPlacesCardFadedOut: () -> Unit
) {
    val locationResetter = Animation(
        locationResetter,
        ::getFadeInAnimation,
        ::getFadeOutAnimation
    )
    val suggestedPlaces = Animation(
        suggestedPlaces,
        ::getFadeInAnimation,
        ::getFadeOutAnimation,
    )
    val suggestedPlacesCard = Animation(
        suggestedPlacesCard,
        ::getFadeInAnimation,
        ::getFadeOutAnimation,
        onSuggestedPlacesCardFadedOut
    )
    val overviewedPlacesCard = Animation(
        overviewedPlacesCard,
        ::getFadeInAnimation,
        ::getFadeOutAnimation,
    )
    val emptySuggestedPlacesInfo = Animation(
        emptySuggestedPlacesInfo,
        ::getFadeInAnimation,
        ::getFadeOutAnimation,
    )
    val emptyOverviewedPlacesInfo = Animation(
        emptyOverviewedPlacesInfo,
        ::getFadeInAnimation,
        ::getFadeOutAnimation,
    )

    class Animation(
        private val view: View,
        getFadeIn: (View) -> AlphaAnimation,
        getFadeOut: (View, (() -> Unit)?) -> AlphaAnimation,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        private val fadeIn = getFadeIn(view)
        private val fadeOut = getFadeOut(view, onAnimationEnd)

        fun fadeIn() {
            view.startAnimation(fadeIn)
        }

        fun fadeOut() {
            view.startAnimation(fadeOut)
        }
    }

    private fun getFadeInAnimationListener(view: View) = getAnimationListener(
        { if (view is ImageButton) view.isEnabled = true },
        { view.visibility = View.VISIBLE }
    )

    private fun getFadeOutAnimationListener(
        view: View,
        onAnimationEnd: (() -> Unit)? = null
    ) = getAnimationListener(
        { if (view is ImageButton) view.isEnabled = false },
        {
            view.visibility = View.INVISIBLE
            if (view is MaterialCardView) onAnimationEnd?.invoke()
        }
    )

    private fun getAnimationListener(
        onAnimationStart: () -> Unit,
        onAnimationEnd: () -> Unit
    ) = object : AnimationListener {
        override fun onAnimationStart(animation: android.view.animation.Animation?) {
            onAnimationStart()
        }

        override fun onAnimationEnd(animation: android.view.animation.Animation?) {
            onAnimationEnd()
        }

        override fun onAnimationRepeat(animation: android.view.animation.Animation?) {
        }
    }

    private fun AlphaAnimation.setUp(
        view: View,
        animationListener: AnimationListener
    ): AlphaAnimation {
        interpolator = LinearInterpolator()
        duration = view.resources.getInteger(R.integer.anim_time).toLong()
        setAnimationListener(animationListener)
        return this
    }

    private fun getFadeInAnimation(
        view: View,
    ) = AlphaAnimation(0f, 1f).setUp(view, getFadeInAnimationListener(view))

    private fun getFadeOutAnimation(
        view: View,
        onAnimationEnd: (() -> Unit)? = null
    ) = AlphaAnimation(1f, 0f).setUp(view, getFadeOutAnimationListener(view, onAnimationEnd))
}