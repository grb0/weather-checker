package ba.grbo.weatherchecker.ui.fragments

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ba.grbo.weatherchecker.R
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.databinding.FragmentOverviewBinding
import ba.grbo.weatherchecker.ui.activities.WeatherCheckerActivity
import ba.grbo.weatherchecker.ui.adapters.PlaceAdapter
import ba.grbo.weatherchecker.ui.viewmodels.OverviewViewModel
import ba.grbo.weatherchecker.util.AlphaAnimator
import ba.grbo.weatherchecker.util.Constants.EMPTY_STRING
import ba.grbo.weatherchecker.util.addDivider
import ba.grbo.weatherchecker.util.getColorFromAttribute
import ba.grbo.weatherchecker.util.toDp
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

    private lateinit var alphaAnimator: AlphaAnimator

    @Inject
    lateinit var locale: Locale

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        activity = requireActivity() as WeatherCheckerActivity
        setUpSuggestionsRecyclerView()
        alphaAnimator = AlphaAnimator(
            binding.locationResetter,
            binding.suggestedPlaces,
            binding.suggestedPlacesCard,
            viewModel::resetSuggestedPlaces
        )
        setListeners()
        viewModel.collectFlows()
        return binding.root
    }

    private fun setUpSuggestionsRecyclerView() {
        binding.suggestedPlaces.adapter = PlaceAdapter(viewModel::onSuggestedPlacesChanged)
        addSuggestedPlacesDivider(false)
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

                launch {
                    suggestedPlacesCardShown.collect(::onSuggestedPlacesCardShownChanged)
                }

                launch {
                    loadingSpinnerShown.collect(::onLoadingSpinnerShowChanged)
                }

                launch {
                    suggestedPlacesCShown.collect(::onSuggestedPlacesShownChanged)
                }

                launch {
                    suggestedPlaces.collect(::onSuggestedPlacesChanged)
                }

                launch {
                    scrollSuggestedPlacesToTop.collect {
                        binding.suggestedPlaces.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun onSuggestedPlacesCardShownChanged(suggestedPlacesCardShown: Boolean) {
        if (suggestedPlacesCardShown) alphaAnimator.suggestedPlacesCard.fadeIn()
        else if (!suggestedPlacesCardShown && binding.suggestedPlacesCard.visibility == View.VISIBLE) {
            alphaAnimator.suggestedPlacesCard.fadeOut()
        }
    }

    private fun onSuggestedPlacesShownChanged(suggestedPlacesShown: Boolean) {
        if (suggestedPlacesShown) alphaAnimator.suggestedPlaces.fadeIn()
        else if (!suggestedPlacesShown && binding.suggestedPlaces.visibility == View.VISIBLE) {
            alphaAnimator.suggestedPlaces.fadeOut()
        }
    }

    private fun onLoadingSpinnerShowChanged(loadingSpinnerShown: Boolean) {
        binding.loadingSpinnerLinearLayout.visibility = if (loadingSpinnerShown) View.VISIBLE
        else View.INVISIBLE
    }

    private fun onSuggestedPlacesChanged(suggestedPlaces: List<Place>?) {
        (binding.suggestedPlaces.adapter as PlaceAdapter).submitList(suggestedPlaces)
    }

    private fun onLocationSearcherFocused() {
        setOnScreenTouchedListener()
        modifySuggestedPlacesCardStroke(2f.toDp(resources), R.attr.colorPrimary)
        modifySuggestedPlacesScrollbar(requireContext().getColorFromAttribute(R.attr.colorPrimary))
        addSuggestedPlacesDivider(true)
    }

    private fun onLocationSearcherUnfocused() {
        removeOnScreenTouchedListener()
        modifySuggestedPlacesCardStroke(1f.toDp(resources), android.R.attr.textColorHint)
        modifySuggestedPlacesScrollbar(ContextCompat.getColor(requireContext(), R.color.scrollbar))
        addSuggestedPlacesDivider(false)
        hideKeyboard()
    }

    private fun addSuggestedPlacesDivider(focused: Boolean) {
        if (binding.suggestedPlaces.itemDecorationCount > 0) {
            for (i in 0 until binding.suggestedPlaces.itemDecorationCount) {
                binding.suggestedPlaces.removeItemDecorationAt(i)
            }
        }
        binding.suggestedPlaces.addDivider(
            if (focused) R.drawable.divider_suggestions_focused
            else R.drawable.divider_suggestions
        )
    }

    private fun modifySuggestedPlacesCardStroke(width: Int, color: Int) {
        binding.suggestedPlacesCard.strokeWidth = width
        binding.suggestedPlacesCard.strokeColor = requireContext().getColorFromAttribute(color)
    }

    private fun modifySuggestedPlacesScrollbar(color: Int) {
        binding.suggestedPlaces.scrollBarColor = color
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
            if (it) alphaAnimator.locationResetter.fadeIn()
            else alphaAnimator.locationResetter.fadeOut()
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

        val isLocationSearcherTouched = isPointInsideViewBounds(
            binding.locationSearcher,
            touchPoint
        )

        val isSuggestionsTouched = isPointInsideViewBounds(
            binding.suggestedPlaces,
            touchPoint
        )

        viewModel.onScreenTouched(isLocationSearcherTouched, isSuggestionsTouched)
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
}