package ba.grbo.weatherchecker.ui.activities

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.marginTop
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ba.grbo.weatherchecker.R
import ba.grbo.weatherchecker.databinding.ActivityWeatherCheckerBinding
import ba.grbo.weatherchecker.di.MainDispatcher
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState
import ba.grbo.weatherchecker.ui.viewmodels.WeatherCheckerViewModel.AnimationState.*
import ba.grbo.weatherchecker.util.BannerAnimator
import ba.grbo.weatherchecker.util.setCustomTopMargin
import ba.grbo.weatherchecker.util.toPixels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class WeatherCheckerActivity : AppCompatActivity() {
    private val viewModel: WeatherCheckerViewModel by viewModels()
    var onScreenTouchedListener: ((event: MotionEvent) -> Unit)? = null

    private lateinit var binding: ActivityWeatherCheckerBinding

    @MainDispatcher
    @Inject
    lateinit var mainDispatcher: MainCoroutineDispatcher

    private lateinit var bannerAnimator: BannerAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WeatherChecker)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weather_checker)
        setListeners()
        initBannerAnimator()
        viewModel.collectFlows()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) onScreenTouchedListener?.invoke(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun blinkBanner() = bannerAnimator.blink()

    fun requestBannerBlink() = viewModel.onBlinkBannerRequested()

    private fun setListeners() {
        binding.weatherCheckerSwipeRefreshLayout.setOnRefreshListener {
            viewModel.onRefreshRequested()
        }
    }

    private fun WeatherCheckerViewModel.collectFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    internetStatus.collectLatest { hasInternet ->
                        // For smooth transition from wifi to cellular, to avoid showing no connection
                        // for a brief moment.
                        delay(300)
                        viewModel.onInternetStatusChanged(hasInternet)
                    }
                }

                launch {
                    // Make sure bannerAnimator is initialized first
                    while (!::bannerAnimator.isInitialized) delay(25)
                    internetMissingBannerAnimationState.collect(::onInternetMissingBannerAnimationStateChanged)
                }

                launch {
                    requestedRefreshDone.collect {
                        binding.weatherCheckerSwipeRefreshLayout.isRefreshing = false
                    }
                }

                launch {
                    blinkInternetMissingBanner.collect { blinkBanner() }
                }
            }
        }
    }

    private fun onInternetMissingBannerAnimationStateChanged(animationState: AnimationState) {
        when (animationState) {
            ANIMATING -> lifecycleScope.launchWhenStarted { bannerAnimator.onAnimating() }
            ANIMATING_INTERRUPTED -> {
                lifecycleScope.launchWhenStarted { bannerAnimator.onAnimatingInterrupted() }
            }
            REVERSE_ANIMATING -> {
                lifecycleScope.launchWhenStarted { bannerAnimator.onReverseAnimating() }
            }
            REVERSE_ANIMATING_INTERRUPTED -> {
                lifecycleScope.launchWhenStarted { bannerAnimator.onReverseAnimatingInterrupted() }
            }
            ANIMATED,
            REVERSE_ANIMATED_WITH_INTERRUPTION -> {
                // To avoid animating on configuration change, which one cannot see, just
                // set Views in their final positions
                showInternetMissingBannerAndShrinkFragmentContainer()
            }
            else -> {
                // Do nothing forREADY, ANIMATED_WITH_INTERRUPTION & REVERSE_ANIMATED
            }
        }
    }

    private fun showInternetMissingBannerAndShrinkFragmentContainer() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (!ViewCompat.isLaidOut(binding.internetMissingBanner)) delay(25)
            if (!isBannerShown()) withContext(mainDispatcher) {
                showInternetMissingBanner()
            }
        }
    }

    private fun isBannerShown(): Boolean {
        return binding.internetMissingBanner.marginTop == 16f.toPixels(resources).roundToInt()
    }

    private fun showInternetMissingBanner() {
        binding.internetMissingBanner.setCustomTopMargin(16f.toPixels(resources))
    }

    private fun initBannerAnimator() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Wait until banner is laid out so its height it ready to be read
            while (!ViewCompat.isLaidOut(binding.internetMissingBanner)) delay(25)
            bannerAnimator = BannerAnimator(
                binding.internetMissingBanner,
                BannerAnimator.DoOnEnd(
                    viewModel::onAnimated,
                    viewModel::onReverseAnimated,
                    viewModel::onAnimatingInterrupted,
                    viewModel::onReverseAnimatingInterrupted
                )
            )
        }
    }
}