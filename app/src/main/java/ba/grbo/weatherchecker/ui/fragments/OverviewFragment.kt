package ba.grbo.weatherchecker.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import ba.grbo.weatherchecker.R
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.databinding.FragmentOverviewBinding
import ba.grbo.weatherchecker.ui.activities.WeatherCheckerActivity
import ba.grbo.weatherchecker.ui.adapters.OverviewedPlaceAdapter
import ba.grbo.weatherchecker.ui.adapters.SuggestedPlaceAdapter
import ba.grbo.weatherchecker.ui.viewmodels.OverviewViewModel
import ba.grbo.weatherchecker.util.*
import ba.grbo.weatherchecker.util.Constants.EMPTY_STRING
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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
        binding = FragmentOverviewBinding.inflate(inflater, container, false).apply {
            locationSearcher.setHintTextColor(
                ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_enabled),
                        intArrayOf(-android.R.attr.state_enabled)
                    ),
                    intArrayOf(
                        requireContext().getColorFromAttribute(android.R.attr.textColorHint),
                        ContextCompat.getColor(requireContext(), R.color.disabled)
                    )
                )
            )
        }
        activity = requireActivity() as WeatherCheckerActivity
        setUpOverviewedPlacesRecyclerView()
        setUpSuggestedPlacesRecyclerView()
        alphaAnimator = AlphaAnimator(
            binding.locationResetter,
            binding.suggestedPlaces,
            binding.suggestedPlacesCard,
            binding.overviewedPlacesCard,
            binding.emptySuggestedPlaces,
            binding.emptyOverviewedPlaces,
            viewModel::resetSuggestedPlaces
        )
        setListeners()
        viewModel.collectFlows()
        return binding.root
    }

    private fun setUpOverviewedPlacesRecyclerView() {
        binding.overviewedPlaces.run {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val verticalOffset = recyclerView.computeVerticalScrollOffset()
                        .toFloat()
                        .toDp(resources)
                    viewModel.onOverviewedPlacesScrolled(verticalOffset)
                    super.onScrolled(recyclerView, dx, dy)
                }
            })

            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            val itemTouchHelper = ItemTouchHelper(getSimpleCallback())
            itemTouchHelper.attachToRecyclerView(this)

            computeVerticalScrollOffset()

            adapter = OverviewedPlaceAdapter(
                viewModel::onOverviewedPlacesChanged,
                viewModel.onImageLoadingError
            ).apply {
                // This prevents fast down scroll when dragging the first item and
                // scroll up when we drag an item to the top
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        itemCount: Int
                    ) {
                        if (fromPosition == 0 || toPosition == 0) binding.overviewedPlaces.scrollToPosition(
                            0
                        )
                    }
                })
            }

            addItemDecoration(
                VerticalSpacingItemDecoration(16f.toDp(resources))
            )
        }
    }

    private fun getSimpleCallback(): SimpleCallback = object : SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
        ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
    ) {
        var startFromPosition: Int? = null
        var endToPosition: Int? = null
        var lastActionState: Int = Int.MAX_VALUE

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition.also { endToPosition = it }
            // Manually updating list and notifying the adapter to avoid updating db on each move
            // which doesn't work well, since db updating speed cannot keep up with moving.
            (binding.overviewedPlaces.adapter as OverviewedPlaceAdapter).run {
                val current = currentList.toMutableList()
                Collections.swap(current, fromPosition, toPosition)
                submitList(current)
            }
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.onOverviewedPlacesSwiped(viewHolder.adapterPosition)
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    viewModel.onDraggingStarted()
                    startFromPosition = viewHolder?.adapterPosition
                    lastActionState = ItemTouchHelper.ACTION_STATE_DRAG
                }
                ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    lastActionState = ItemTouchHelper.ACTION_STATE_SWIPE
                }
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    if (lastActionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        startFromPosition?.let { from ->
                            endToPosition?.let { to ->
                                viewModel.onOverviewedPlacesMoved(from, to)
                                viewModel.onDraggingFinished()
                            }
                        }
                    }
                }
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            RecyclerViewSwipeDecorator(
                c,
                recyclerView,
                viewHolder,
                dX,
                actionState
            ).decorate()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun setUpSuggestedPlacesRecyclerView() {
        binding.suggestedPlaces.adapter = SuggestedPlaceAdapter(
            viewModel::onSuggestedPlacesChanged,
            viewModel::onSuggestedPlaceClicked,
            MutableStateFlow(requireContext().getColorFromAttribute(android.R.attr.textColorHint)),
            viewLifecycleOwner
        )
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
                    locationSearcherEnabled.collect(::onLocationSearchedEnabledChanged)
                }

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
                    clearLocationSearcherFocus.collect {
                        binding.locationSearcher.clearFocus()
                    }
                }

                launch {
                    unfocusLocationSearcher.collect { unfocusLocationSearcher() }
                }

                launch {
                    suggestedPlacesCardShown.collect(::onSuggestedPlacesCardShownChanged)
                }

                launch {
                    suggestedPlacesLoadingSpinnerShown.collect(::onSuggestedPlacesLoadingSpinnerShownChanged)
                }

                launch {
                    overviewedPlacesLoadingSpinnerShown.collect(::onOverviewedPlacesLoadingSpinnerShownChanged)
                }

                launch {
                    suggestedPlacesCShown.collect(::onSuggestedPlacesShownChanged)
                }

                launch {
                    updateSuggestedPlaceAdapter.collect(::onInternetStatusChanged)
                }

                launch {
                    overviewedPlaces.collect(::onOverviewedPlacesChanged)
                }

                launch {
                    suggestedPlaces.collect(::onSuggestedPlacesChanged)
                }

                launch {
                    scrollOverviewedPlacesToTop.collect {
                        if (!binding.overviewedPlaces.canScrollVertically(-1)) {
                            binding.overviewedPlaces.scrollToPosition(0)
                        } else lifecycleScope.launch {
                            delay(300) // To make it more natural
                            binding.overviewedPlaces.smoothScrollToPosition(0)
                        }
                    }
                }

                launch {
                    scrollSuggestedPlacesToTop.collect {
                        binding.suggestedPlaces.scrollToPosition(0)
                    }
                }

                launch {
                    exceptionSnackbarShown.collectLatest {
                        if (it) showSnackbar(
                            R.string.exception_msg,
                            R.string.exception_action_msg
                        ) {
                            viewModel.onExceptionSnackbarMessageAcknowledge()
                        }
                    }
                }

                launch {
                    undoRemovedOverviewedPlaceSnackbackShown.collectLatest { message ->
                        message?.let {
                            showUndoSnackbar(
                                getString(R.string.undo_msg, it),
                                R.string.undo_action_msg
                            )
                        }
                    }
                }

                launch {
                    blinkInternetMissingBanner.collect { activity.requestBannerBlink() }
                }

                launch {
                    overviewedPlacesCardShown.collect(::onOverviewedPlacesCardShownChanged)
                }

                launch {
                    emptyOverviewedPlacesInfoShown.collect(::onEmptyOverviewedPlacesInfoShownChanged)
                }

                launch {
                    emptySuggestedPlacesInfoShown.collect(::onEmptySuggestedPlacesInfoShownChanged)
                }

                launch {
                    verticalDividerShown.collect(::onVerticalDividerShownChanged)
                }

                launch {
                    swipeToRefreshEnabled.collect { enabled ->
                        enabled?.let {
                            activity.requestSwipeToRefreshEnabledStateChange(enabled)
                        }
                    }
                }

                launch {
                    suggestedPlacesEnabled.collect { enabled ->
                        enabled?.let {
                            (binding.suggestedPlaces.adapter as SuggestedPlaceAdapter).enabled.value = it
                        }
                    }
                }
            }
        }
    }

    private fun onVerticalDividerShownChanged(verticalDividerShown: Boolean?) {
        verticalDividerShown?.let {
            binding.verticalDivider.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun onViewShownChanged(
        shown: Boolean,
        fadeIn: () -> Unit,
        fadeOut: () -> Unit
    ) = if (shown) fadeIn() else fadeOut()

    private fun onLocationSearchedEnabledChanged(locationSearcherShown: Boolean?) {
        locationSearcherShown?.let { binding.locationSearcher.isEnabled = it }
    }

    private fun onEmptySuggestedPlacesInfoShownChanged(emptyInfoShown: Boolean?) {
        emptyInfoShown?.let {
            onViewShownChanged(
                emptyInfoShown,
                alphaAnimator.emptySuggestedPlacesInfo::fadeIn,
                alphaAnimator.emptySuggestedPlacesInfo::fadeOut
            )
        }
    }

    private fun onEmptyOverviewedPlacesInfoShownChanged(emptyInfoShown: Boolean?) {
        emptyInfoShown?.let {
            onViewShownChanged(
                emptyInfoShown,
                alphaAnimator.emptyOverviewedPlacesInfo::fadeIn,
                alphaAnimator.emptyOverviewedPlacesInfo::fadeOut
            )
        }
    }

    private fun onOverviewedPlacesCardShownChanged(overviewedPlacesCardShown: Boolean?) {
        overviewedPlacesCardShown?.let {
            onViewShownChanged(
                overviewedPlacesCardShown,
                alphaAnimator.overviewedPlacesCard::fadeIn,
                alphaAnimator.overviewedPlacesCard::fadeOut
            )
        }
    }

    private fun onSuggestedPlacesCardShownChanged(suggestedPlacesCardShown: Boolean?) {
        suggestedPlacesCardShown?.let {
            onViewShownChanged(
                suggestedPlacesCardShown,
                alphaAnimator.suggestedPlacesCard::fadeIn,
                alphaAnimator.suggestedPlacesCard::fadeOut
            )
        }
    }

    private fun onSuggestedPlacesShownChanged(suggestedPlacesShown: Boolean?) {
        suggestedPlacesShown?.let {
            onViewShownChanged(
                suggestedPlacesShown,
                alphaAnimator.suggestedPlaces::fadeIn,
                alphaAnimator.suggestedPlaces::fadeOut
            )
        }
    }

    private fun showSnackbar(
        @StringRes message: Int,
        @StringRes actionMsg: Int,
        onActionClicked: () -> Unit
    ) {
        Snackbar.make(binding.overviewConstraintLayout, message, Snackbar.LENGTH_INDEFINITE)
            .apply {
                setAction(actionMsg) {
                    dismiss()
                    onActionClicked()
                }

            }.show()
    }

    private fun showUndoSnackbar(
        message: String,
        @StringRes actionMsg: Int
    ) {
        Snackbar.make(binding.overviewConstraintLayout, message, Snackbar.LENGTH_LONG)
            .apply {
                setAction(actionMsg) {
                    viewModel.onUndoRemovedOverviewedPlace()
                }
                addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        viewModel.onUndoSnackbarDismissed()
                        super.onDismissed(transientBottomBar, event)
                    }
                })
            }
            .show()
    }

    private fun onSuggestedPlacesLoadingSpinnerShownChanged(loadingSpinnerShown: Boolean?) {
        loadingSpinnerShown?.let {
            onLoadingSpinnerShownChanged(
                binding.suggestedPlacesLoadingSpinner,
                loadingSpinnerShown
            )
        }
    }

    private fun onOverviewedPlacesLoadingSpinnerShownChanged(loadingSpinnerShown: Boolean?) {
        loadingSpinnerShown?.let {
            onLoadingSpinnerShownChanged(
                binding.overviewedPlacesLoadingSpinner,
                loadingSpinnerShown
            )
        }
    }

    private fun onLoadingSpinnerShownChanged(view: View, loadingSpinnerShown: Boolean) {
        view.visibility = if (loadingSpinnerShown) View.VISIBLE else View.INVISIBLE
    }

    private fun onInternetStatusChanged(hasInternet: Boolean?) {
        hasInternet?.let {
            (binding.suggestedPlaces.adapter as SuggestedPlaceAdapter).hasInternet.value = it
        }
    }

    private fun onSuggestedPlacesChanged(suggestedPlaces: List<Place>?) {
        (binding.suggestedPlaces.adapter as SuggestedPlaceAdapter).submitList(suggestedPlaces)
    }

    private fun onOverviewedPlacesChanged(overviewedPlaces: List<Place>?) {
        (binding.overviewedPlaces.adapter as OverviewedPlaceAdapter).submitList(overviewedPlaces)
    }

    private fun onLocationSearcherFocused() {
        setOnScreenTouchedListener()
        val color = requireContext().getColorFromAttribute(R.attr.colorPrimary)
        modifySuggestedPlacesCardStroke(2f.toDp(resources), color)
        modifySuggestedPlacesScrollbar(color)
        setRippleColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.ripple
            )
        )
        addSuggestedPlacesDivider(true)
    }

    @Suppress("NotifyDataSetChanged")
    private fun setRippleColor(color: Int) {
        (binding.suggestedPlaces.adapter as SuggestedPlaceAdapter).rippleColor.value = color
    }

    private fun onLocationSearcherUnfocused() {
        removeOnScreenTouchedListener()
        val color = requireContext().getColorFromAttribute(android.R.attr.textColorHint)
        modifySuggestedPlacesCardStroke(1f.toDp(resources), color)
        modifySuggestedPlacesScrollbar(
            ContextCompat.getColor(
                requireContext(),
                R.color.scrollbar
            )
        )
        addSuggestedPlacesDivider(false)
        setRippleColor(color)
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
        binding.suggestedPlacesCard.strokeColor = color
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