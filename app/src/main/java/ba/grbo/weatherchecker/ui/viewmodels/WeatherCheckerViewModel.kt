package ba.grbo.weatherchecker.ui.viewmodels

import androidx.lifecycle.ViewModel
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState.*
import ba.grbo.weatherchecker.util.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WeatherCheckerViewModel @Inject constructor(networkManager: NetworkManager) : ViewModel() {
    val internetStatus = networkManager.internetStatus

    private val _internetMissingBannerAnimationState = MutableStateFlow(READY)
    val internetMissingBannerAnimationState: StateFlow<AnimationState>
        get() = _internetMissingBannerAnimationState

    enum class AnimationState {
        READY,
        ANIMATING,
        ANIMATING_INTERRUPTED,
        ANIMATED,
        ANIMATED_WITH_INTERRUPTION,
        REVERSE_ANIMATING,
        REVERSE_ANIMATING_INTERRUPTED,
        REVERSE_ANIMATED,
        REVERSE_ANIMATED_WITH_INTERRUPTION
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    fun onInternetStatusChanged(hasInternet: Boolean) {
        when (hasInternet) {
            false -> when (_internetMissingBannerAnimationState.value) {
                READY,
                ANIMATED_WITH_INTERRUPTION,
                REVERSE_ANIMATED -> _internetMissingBannerAnimationState.value = ANIMATING
                ANIMATING_INTERRUPTED,
                REVERSE_ANIMATING -> {
                    _internetMissingBannerAnimationState.value = REVERSE_ANIMATING_INTERRUPTED
                }
            }
            true -> when (_internetMissingBannerAnimationState.value) {
                ANIMATED,
                REVERSE_ANIMATED_WITH_INTERRUPTION -> {
                    _internetMissingBannerAnimationState.value = REVERSE_ANIMATING
                }
                REVERSE_ANIMATING_INTERRUPTED,
                ANIMATING -> _internetMissingBannerAnimationState.value = ANIMATING_INTERRUPTED
            }
        }
    }

    fun onAnimated() {
        _internetMissingBannerAnimationState.value = ANIMATED
    }

    fun onReverseAnimated() {
        _internetMissingBannerAnimationState.value = REVERSE_ANIMATED
    }

    fun onAnimatingInterrupted() {
        _internetMissingBannerAnimationState.value = ANIMATED_WITH_INTERRUPTION
    }

    fun onReverseAnimatingInterrupted() {
        _internetMissingBannerAnimationState.value = REVERSE_ANIMATED_WITH_INTERRUPTION
    }
}