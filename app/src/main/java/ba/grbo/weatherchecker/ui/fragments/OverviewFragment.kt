package ba.grbo.weatherchecker.ui.fragments

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ba.grbo.weatherchecker.databinding.FragmentOverviewBinding
import ba.grbo.weatherchecker.ui.activities.WeatherCheckerActivity
import ba.grbo.weatherchecker.ui.viewmodels.OverviewViewModel
import ba.grbo.weatherchecker.util.Constants.EMPTY_STRING
import ba.grbo.weatherchecker.util.setUp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class OverviewFragment : Fragment() {
    private val viewModel: OverviewViewModel by viewModels()
    private lateinit var binding: FragmentOverviewBinding
    private lateinit var activity: WeatherCheckerActivity

    private val fadeIn: AlphaAnimation by lazy { AlphaAnimation(0f, 1f).setUp(resources) }
    private val fadeOut: AlphaAnimation by lazy { AlphaAnimation(1f, 0f).setUp(resources) }

    @Inject
    lateinit var locale: Locale

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        activity = requireActivity() as WeatherCheckerActivity
        setListeners()
        viewModel.collectFlows()
        return binding.root
    }

    private fun setListeners() {
        binding.run {
            locationSearcher.run {
                onKeyUpListener = { viewModel.onKeyboardHidden() }
                setOnFocusChangeListener { _, hasFocus ->
                    viewModel.onLocationSearcherFocusChanged(hasFocus)
                }
                addTextChangedListener {
                    it?.let { viewModel.onLocationSearcherTextChanged(it.toString()) }
                }
            }

            locationResetter.setOnClickListener { viewModel.onLocationResetterClicked() }
        }
    }

    private fun OverviewViewModel.collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    onLocationSearcherFocusChanged.collect {
                        it?.let { hasFocus ->
                            if (hasFocus) onLocationSearcherFocused()
                            else if (!hasFocus && viewModel.userInitializedUnfocus) {
                                onLocationSearcherUnfocused()
                            }
                        }
                    }
                }

                launch {
                    locationResetterVisibility.collect(::onLocationResetterVisibilityChanged)
                }

                launch { resetLocationSearcherText.collect { resetLocationSearcherText() } }

                launch {
                    unfocusLocationSearcher.collect { unfocusLocationSearcher() }
                }
            }
        }
    }

    private fun onLocationSearcherFocused() {
        setOnScreenTouchedListener()
    }

    private fun onLocationSearcherUnfocused() {
        removeOnScreenTouchedListener()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getInputMethodManager(requireContext())
        imm.hideSoftInputFromWindow(binding.locationSearcher.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm = getInputMethodManager(requireContext())
        imm.showSoftInput(binding.locationSearcher, 0)
    }

    private fun getInputMethodManager(context: Context): InputMethodManager {
        return context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun onLocationResetterVisibilityChanged(visible: Boolean?) {
        visible?.let {
            if (it) setAnimationListenerAndStartAnimation(
                binding.locationResetter,
                fadeIn,
                true
            ) else setAnimationListenerAndStartAnimation(
                binding.locationResetter,
                fadeOut,
                false
            )
        }
    }

    private fun resetLocationSearcherText() {
        binding.locationSearcher.setText(EMPTY_STRING)
        binding.locationSearcher.requestFocus()
        showKeyboard()
    }

    private fun unfocusLocationSearcher() {
        binding.locationSearcher.clearFocus()
    }

    private fun setOnScreenTouchedListener() {
        activity.onScreenTouchedListener = { event -> onScreenTouched(event) }
    }

    private fun removeOnScreenTouchedListener() {
        activity.onScreenTouchedListener = null
        viewModel.onScreenTouchedListenerRemoved()
    }

    private fun onScreenTouched(event: MotionEvent) {
        val touchPoint = Point(event.rawX.roundToInt(), event.rawY.roundToInt())

        viewModel.onScreenTouched(
            isPointInsideViewBounds(
                binding.locationSearcher,
                touchPoint
            )
        )
    }

    private fun isPointInsideViewBounds(view: View, point: Point): Boolean = Rect().run {
        // Get view rectangle
        view.getDrawingRect(this)

        // Apply offset
        IntArray(2).also { locationOnScreen ->
            view.getLocationOnScreen(locationOnScreen)
            offset(locationOnScreen[0], locationOnScreen[1])
        }

        // Check if rectangle contains point
        contains(point.x, point.y)
    }

    private fun setAnimationListenerAndStartAnimation(
        view: View,
        animation: AlphaAnimation,
        fadingIn: Boolean
    ) {
        animation.setAnimationListener(
            getAnimationListener(
                view,
                fadingIn,
                fadeIn,
                fadeOut
            )
        )
        view.startAnimation(animation)
    }

    private fun getAnimationListener(
        view: View,
        fadingIn: Boolean,
        fadeIn: AlphaAnimation,
        fadeOut: AlphaAnimation
    ) = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            if (!fadingIn && view is ImageButton) view.isEnabled = false
            else if (view is ImageButton) view.isEnabled = true
        }

        override fun onAnimationEnd(animation: Animation?) {
            if (fadingIn) {
                view.visibility = View.VISIBLE
                fadeIn.setAnimationListener(null)
            } else {
                view.visibility = View.INVISIBLE
                fadeOut.setAnimationListener(null)
            }
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    }
}