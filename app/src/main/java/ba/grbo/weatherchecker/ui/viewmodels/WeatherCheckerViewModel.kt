package ba.grbo.weatherchecker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.weatherchecker.data.source.Repository
import ba.grbo.weatherchecker.di.IODispatcher
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState.*
import ba.grbo.weatherchecker.util.NetworkManager
import ba.grbo.weatherchecker.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherCheckerViewModel @Inject constructor(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val networkManager: NetworkManager,
    private val repository: Repository
) : ViewModel() {
    val internetStatus = networkManager.internetStatus

    private val _internetMissingBannerAnimationState = MutableStateFlow(READY)
    val internetMissingBannerAnimationState: StateFlow<AnimationState>
        get() = _internetMissingBannerAnimationState

    private val _blinkInternetMissingBanner = SingleSharedFlow<Unit>()
    val blinkInternetMissingBanner: SharedFlow<Unit>
        get() = _blinkInternetMissingBanner

    private val _requestedRefreshDone = SingleSharedFlow<Unit>()
    val requestedRefreshDone: SharedFlow<Unit>
        get() = _requestedRefreshDone

    private val _swipeToRefreshEnabled = MutableStateFlow<Boolean?>(null)
    val swipeToRefreshEnabled: StateFlow<Boolean?>
        get() = _swipeToRefreshEnabled

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

    fun onRefreshRequested() {
        viewModelScope.refreshOverviewedPlaces()
    }

    fun onBlinkBannerRequested() {
        _blinkInternetMissingBanner.tryEmit(Unit)
    }

    fun onSwipeToRefreshEnabledChanged(enabled: Boolean) {
        _swipeToRefreshEnabled.value = enabled
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

    private fun CoroutineScope.refreshOverviewedPlaces() = launch(ioDispatcher) {
        if (networkManager.hasInternet) repository.refreshOverviewedPlaces(::requestedRefreshDone)
        else {
            requestedRefreshDone()
            onBlinkBannerRequested()
        }
    }

    private fun requestedRefreshDone() {
        _requestedRefreshDone.tryEmit(Unit)
    }
}