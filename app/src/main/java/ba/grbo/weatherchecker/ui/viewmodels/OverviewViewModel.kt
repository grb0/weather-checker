package ba.grbo.weatherchecker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.weatherchecker.data.models.local.Place
import ba.grbo.weatherchecker.data.source.Repository
import ba.grbo.weatherchecker.data.source.Result.Loading
import ba.grbo.weatherchecker.data.source.Result.SourceResult.Error
import ba.grbo.weatherchecker.data.source.Result.SourceResult.Success
import ba.grbo.weatherchecker.di.IODispatcher
import ba.grbo.weatherchecker.util.Constants.SEARCHER_DEBOUNCE_PERIOD
import ba.grbo.weatherchecker.util.NetworkManager
import ba.grbo.weatherchecker.util.OnImageLoadingError
import ba.grbo.weatherchecker.util.SingleSharedFlow
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val repository: Repository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val networkManager: NetworkManager
) : ViewModel() {
    val onImageLoadingError: OnImageLoadingError = OnImageLoadingError {
        notifyUserOfError(Exception(it))
    }

    private val internetStatus = networkManager.internetStatus

    private var _userInitializedUnfocus = false
    val userInitializedUnfocus: Boolean
        get() = _userInitializedUnfocus

    private val location = MutableStateFlow<String?>(null)

    private val locationWithInternetStatus = location.combine(internetStatus) { l, i -> l to i }

    private val _locationSearchedEnabled = MutableStateFlow<Boolean?>(null)
    val locationSearcherEnabled: StateFlow<Boolean?>
        get() = _locationSearchedEnabled

    private val _onLocationSearcherFocusChanged = MutableStateFlow<Boolean?>(null)
    val onLocationSearcherFocusChanged: StateFlow<Boolean?>
        get() = _onLocationSearcherFocusChanged

    private val _unfocusLocationSearcher = SingleSharedFlow<Unit>()
    val unfocusLocationSearcher: SharedFlow<Unit>
        get() = _unfocusLocationSearcher

    private val _locationResetterVisibility = MutableStateFlow<Boolean?>(null)
    val locationResetterVisibility: StateFlow<Boolean?>
        get() = _locationResetterVisibility

    private val _resetLocationSearcherText = SingleSharedFlow<Unit>()
    val resetLocationSearcherText: SharedFlow<Unit>
        get() = _resetLocationSearcherText

    private val _suggestedPlaces = MutableStateFlow<List<Place>?>(null)
    val suggestedPlaces: StateFlow<List<Place>?>
        get() = _suggestedPlaces

    private val _overviewedPlaces = MutableStateFlow<List<Place>?>(null)
    val overviewedPlaces: StateFlow<List<Place>?>
        get() = _overviewedPlaces

    private val _suggestedPlacesCardShown = MutableStateFlow<Boolean?>(null)
    val suggestedPlacesCardShown: StateFlow<Boolean?>
        get() = _suggestedPlacesCardShown

    private val _suggestedPlacesShown = MutableStateFlow<Boolean?>(null)
    val suggestedPlacesCShown: StateFlow<Boolean?>
        get() = _suggestedPlacesShown

    private val _overviewedPlacesCardShown = MutableStateFlow<Boolean?>(null)
    val overviewedPlacesCardShown: StateFlow<Boolean?>
        get() = _overviewedPlacesCardShown

    private val _emptySuggestedPlacesInfoShown = MutableStateFlow<Boolean?>(null)
    val emptySuggestedPlacesInfoShown: StateFlow<Boolean?>
        get() = _emptySuggestedPlacesInfoShown

    private val _emptyOverviewedPlacesInfoShown = MutableStateFlow<Boolean?>(null)
    val emptyOverviewedPlacesInfoShown: StateFlow<Boolean?>
        get() = _emptyOverviewedPlacesInfoShown

    private val _suggestedPlacesLoadingSpinnerShown = MutableStateFlow<Boolean?>(null)
    val suggestedPlacesLoadingSpinnerShown: StateFlow<Boolean?>
        get() = _suggestedPlacesLoadingSpinnerShown

    private val _overviewedPlacesLoadingSpinnerShown = MutableStateFlow<Boolean?>(null)
    val overviewedPlacesLoadingSpinnerShown: StateFlow<Boolean?>
        get() = _overviewedPlacesLoadingSpinnerShown

    private val _blinkInternetMissingBanner = SingleSharedFlow<Unit>()
    val blinkInternetMissingBanner: SharedFlow<Unit>
        get() = _blinkInternetMissingBanner

    private val _undoRemovedOverviewedPlaceSnackbackShown = MutableStateFlow<String?>(null)
    val undoRemovedOverviewedPlaceSnackbackShown: StateFlow<String?>
        get() = _undoRemovedOverviewedPlaceSnackbackShown

    private val _exceptionSnackbarShown = MutableStateFlow(false)
    val exceptionSnackbarShown: StateFlow<Boolean>
        get() = _exceptionSnackbarShown

    private val _scrollOverviewedPlacesToTop = SingleSharedFlow<Unit>()
    val scrollOverviewedPlacesToTop: SharedFlow<Unit>
        get() = _scrollOverviewedPlacesToTop

    private val _scrollSuggestedPlacesToTop = SingleSharedFlow<Unit>()
    val scrollSuggestedPlacesToTop: SharedFlow<Unit>
        get() = _scrollSuggestedPlacesToTop

    private val _clearLocationSearcherFocus = SingleSharedFlow<Unit>()
    val clearLocationSearcherFocus: SharedFlow<Unit>
        get() = _clearLocationSearcherFocus

    private val _verticalDividerShown = MutableStateFlow<Boolean?>(null)
    val verticalDividerShown: StateFlow<Boolean?>
        get() = _verticalDividerShown

    private val _updateSuggestedPlaceAdapter = MutableStateFlow<Boolean?>(false)
    val updateSuggestedPlaceAdapter: StateFlow<Boolean?>
        get() = _updateSuggestedPlaceAdapter

    private val _swipeToRefreshEnabled = MutableStateFlow<Boolean?>(null)
    val swipeToRefreshEnabled: StateFlow<Boolean?>
        get() = _swipeToRefreshEnabled

    private val _suggestedPlacesEnabled = MutableStateFlow<Boolean?>(null)
    val suggestedPlacesEnabled: StateFlow<Boolean?>
        get() = _suggestedPlacesEnabled

    private var isVerticalDividerShown = false
    private var wasVerticalDidiverHidden = false

    private var overviewedPlacesSize = 0

    private var removedOverviewedPlace: Place? = null
    private var wasUndone = false

    init {
        viewModelScope.emitOverviewedPlaces()
        viewModelScope.collectLatestOverviewedPlaces()
        viewModelScope.collectLatestLocationWithInternetStatus()
        viewModelScope.collectLatestSuggestedPlaces()
    }

    fun onLocationSearcherFocusChanged(hasFocus: Boolean) {
        _onLocationSearcherFocusChanged.value = hasFocus
    }

    fun onLocationSearcherTextChanged(location: String) {
        this.location.value = location
    }

    fun onLocationResetterClicked() {
        resetLocationSearcherText()
    }

    private fun resetLocationSearcherText() {
        _resetLocationSearcherText.tryEmit(Unit)
    }

    fun onScreenTouched(
        isLocationSearcherTouched: Boolean,
        isSuggestionsTouched: Boolean
    ) {
        if (!isLocationSearcherTouched && !isSuggestionsTouched) requestLocationSearcherUnfocus()
    }

    fun onScreenTouchedListenerRemoved() {
        _userInitializedUnfocus = false
        _onLocationSearcherFocusChanged.value = null
    }

    fun onKeyboardHidden() {
        if (_onLocationSearcherFocusChanged.value != null) requestLocationSearcherUnfocus()
    }

    private fun requestLocationSearcherUnfocus() {
        _userInitializedUnfocus = true
        _unfocusLocationSearcher.tryEmit(Unit)
    }

    private fun CoroutineScope.collectLatestLocationWithInternetStatus() = launch(ioDispatcher) {
        locationWithInternetStatus.collectLatest {
            val (location, hasInternet) = it
            location?.let { loc ->
                if (loc.isNotEmpty()) {
                    showLocationResetter()
                    if (loc.length < 3) repository.resetSuggestedPlaces()
                    else repository.setSuggestedPlacesToLoadingState()
                    delay(SEARCHER_DEBOUNCE_PERIOD)
                    if (loc.length >= 3) {
                        _updateSuggestedPlaceAdapter.value = hasInternet
                        repository.updateSuggestedPlaces(loc, hasInternet)
                    }
                } else {
                    hideLocationResetter()
                    repository.resetSuggestedPlaces()
                }
            }
        }
    }

    private fun CoroutineScope.collectLatestOverviewedPlaces() = launch(ioDispatcher) {
        repository.overviewedPlaces.collectLatest { overviewedPlaces ->
            when (overviewedPlaces) {
                is Success -> onOverviewedPlacesSuccess(overviewedPlaces.data)
                is Error -> onOverviewedPlacesError(overviewedPlaces.exception)
                is Loading -> onOverviewedPlacesLoading()
            }
        }
    }

    private fun CoroutineScope.collectLatestSuggestedPlaces() = launch(ioDispatcher) {
        repository.suggestedPlaces.collectLatest { suggestedPlaces ->
            when (suggestedPlaces) {
                null -> onSuggestedPlacesNull()
                is Success -> onSuggestedPlacesSuccess(suggestedPlaces.data)
                is Error -> onPlacesError(suggestedPlaces.exception)
                is Loading -> onSuggestedPlacesLoading()
            }
        }
    }

    private fun onOverviewedPlacesSuccess(overviewedPlaces: List<Place>) {
        if (overviewedPlaces.isNotEmpty()) onOverviewedPlacesNonEmptySuccess(overviewedPlaces)
        else onOverviewedPlacesEmptySuccess()
    }

    private fun onOverviewedPlacesNonEmptySuccess(overviewedPlaces: List<Place>) {
        overviewedPlacesSize = overviewedPlaces.size
        completelyHideSuggestedPlaces()
        showOnlyOverviewedPlaces()
        showVerticalDividerIfItWasShown()
        setOverviewedPlaces(overviewedPlaces)
        enableLocationSearcher()
    }

    private fun onOverviewedPlacesEmptySuccess() {
        overviewedPlacesSize = 0
        completelyHideSuggestedPlaces()
        hideVerticalDividerIfItsShown()
        showOnlyEmptyOverviewedPlacesInfo()
        enableLocationSearcher()
    }

    private fun onOverviewedPlacesError(exception: Exception) {
        hideOverviewedPlacesLoadingSpinner()
        completelyHideSuggestedPlaces()
        notifyUserOfError(exception)
        enableLocationSearcher()
    }

    private fun onOverviewedPlacesLoading() {
        completelyHideSuggestedPlaces()
        resetLocationSearcherText()
        clearLocationSearcherFocus()
        disableLocationSearcher()
        hideEmptyOverviewedPlacesInfo()
        showOverviewedPlacesLoadingSpinner()
    }

    private fun onSuggestedPlacesNull() {
        completelyHideSuggestedPlaces()
        showOverviewedPlacesCardOrEmptyOverviewedPlacesInfo()
    }

    private fun onSuggestedPlacesSuccess(suggestedPlaces: List<Place>) {
        if (suggestedPlaces.isNotEmpty()) onSuggestedPlacesNonEmptySuccess(suggestedPlaces)
        else onSuggestedPlacesEmptySuccess()
    }

    private fun onSuggestedPlacesNonEmptySuccess(suggestedPlaces: List<Place>) {
        hideVerticalDividerIfItsShown()
        completelyHideOverviewedPlaces()
        showOnlySuggestedPlaces()
        setSuggestedPlaces(suggestedPlaces)
    }

    private fun onSuggestedPlacesEmptySuccess() {
        completelyHideOverviewedPlaces()
        showOnlyEmptySuggestedPlacesInfo()
    }

    private fun onSuggestedPlacesLoading() {
        completelyHideOverviewedPlaces()
        hideVerticalDividerIfItsShown()
        showOnlySuggestedPlacesLoadingSpinner()
    }

    private fun completelyHideSuggestedPlaces() {
        hideSuggestedPlacesLoadingSpinner()
        hideSuggestedPlaces()
        hideEmptySuggestedPlacesInfo()
        hideSuggestedPlacesCard()
    }

    private fun completelyHideOverviewedPlaces() {
        hideOverviewedPlacesCard()
        hideOverviewedPlacesLoadingSpinner()
        hideEmptyOverviewedPlacesInfo()
    }

    private fun showOnlySuggestedPlaces() {
        hideSuggestedPlacesLoadingSpinner()
        hideEmptySuggestedPlacesInfo()
        showSuggestedPlacesCard()
        showSuggestedPlaces()
        enableSuggestedPlaces()
    }

    private fun showOnlyEmptySuggestedPlacesInfo() {
        hideSuggestedPlacesLoadingSpinner()
        hideSuggestedPlaces()
        showEmptySuggestedPlacesInfo()
    }

    private fun showOnlySuggestedPlacesLoadingSpinner() {
        hideSuggestedPlaces()
        hideEmptySuggestedPlacesInfo()
        showSuggestedPlacesCard()
        showSuggestedPlacesLoadingSpinner()
    }

    private fun showOnlyOverviewedPlaces() {
        hideOverviewedPlacesLoadingSpinner()
        hideEmptyOverviewedPlacesInfo()
        showOverviewPlacesCard()
    }

    private fun showOnlyEmptyOverviewedPlacesInfo() {
        hideOverviewedPlacesLoadingSpinner()
        hideOverviewedPlacesCard()
        setOverviewedPlaces(null)
        showEmptyOverviewedPlacesInfo()
    }

    private fun hideVerticalDividerIfItsShown() {
        if (isVerticalDividerShown) {
            _verticalDividerShown.value = false
            wasVerticalDidiverHidden = true
        }
    }

    private fun showVerticalDividerIfItWasShown() {
        if (isVerticalDividerShown) {
            _verticalDividerShown.value = true
            wasVerticalDidiverHidden = false
        }
    }

    private fun showOverviewedPlacesCardOrEmptyOverviewedPlacesInfo() {
        if (_overviewedPlacesLoadingSpinnerShown.value != true) {
            if (_overviewedPlaces.value?.isNotEmpty() == true) {
                showVerticalDividerIfItWasShown()
                showOverviewPlacesCard()
            } else showEmptyOverviewedPlacesInfo()
        }
    }

    private fun CoroutineScope.emitOverviewedPlaces() = launch(ioDispatcher) {
        repository.setOverviewedPlacesToLoading()
        delay(100) // Small delay, to give networkManager enough time to check for internet status
        repository.emitOverviewedPlaces(networkManager.hasInternet)
    }

    private fun setOverviewedPlaces(overviewedPlaces: List<Place>?) {
        _overviewedPlaces.value = overviewedPlaces
    }

    private fun setSuggestedPlaces(suggestedPlaces: List<Place>?) {
        _suggestedPlaces.value = suggestedPlaces
    }

    private fun showOverviewPlacesCard() {
        showView(_overviewedPlacesCardShown)
    }

    private fun hideOverviewedPlacesCard() {
        hideView(_overviewedPlacesCardShown)
    }

    private fun showEmptySuggestedPlacesInfo() {
        showView(_emptySuggestedPlacesInfoShown)
    }

    private fun hideEmptySuggestedPlacesInfo() {
        hideView(_emptySuggestedPlacesInfoShown)
    }

    private fun showEmptyOverviewedPlacesInfo() {
        showView(_emptyOverviewedPlacesInfoShown)
    }

    private fun hideEmptyOverviewedPlacesInfo() {
        hideView(_emptyOverviewedPlacesInfoShown)
    }

    private fun showOverviewedPlacesLoadingSpinner() {
        showView(_overviewedPlacesLoadingSpinnerShown)
    }

    private fun hideOverviewedPlacesLoadingSpinner() {
        hideView(_overviewedPlacesLoadingSpinnerShown)
    }

    private fun showSuggestedPlacesCard() {
        showView(_suggestedPlacesCardShown)
    }

    private fun hideSuggestedPlacesCard() {
        hideView(_suggestedPlacesCardShown)
    }

    private fun showSuggestedPlaces() {
        showView(_suggestedPlacesShown)
    }

    private fun hideSuggestedPlaces() {
        hideView(_suggestedPlacesShown)
    }

    private fun enableLocationSearcher() {
        _locationSearchedEnabled.value = true
    }

    private fun disableLocationSearcher() {
        _locationSearchedEnabled.value = false
    }

    private fun showView(view: MutableStateFlow<Boolean?>) {
        view.value = true
    }

    private fun hideView(view: MutableStateFlow<Boolean?>) {
        view.value = false
    }

    // According to the exception caught we can show an appropriate message, however for
    // simplicity we're gonna show a generic message.
    @Suppress("UNUSED_PARAMETER")
    private fun notifyUserOfError(exception: Exception) {
        _exceptionSnackbarShown.value = true
        Logger.i("original: $exception")
        exception.suppressed.forEach {
            Logger.i("suppressed: $it")
        }
    }

    private fun onPlacesError(exception: Exception) = when (exception) {
        is HttpException -> {
            hideSuggestedPlacesLoadingSpinner()
            hideSuggestedPlaces()
            showEmptySuggestedPlacesInfo()
        }
        else -> {
            hideSuggestedPlacesLoadingSpinner()
            hideSuggestedPlaces()
            hideEmptySuggestedPlacesInfo()
            hideSuggestedPlacesCard()
            showVerticalDividerIfItWasShown()
            showOverviewPlacesCard()
            notifyUserOfError(exception)
        }

    }

    fun onExceptionSnackbarMessageAcknowledge() {
        _exceptionSnackbarShown.value = false
    }

    fun onUndoRemovedOverviewedPlace() {
        removedOverviewedPlace?.let {
            viewModelScope.undoRemovedOverviewedPlace(it) {
                wasUndone = true
                removedOverviewedPlace = null
            }
        }
    }

    fun onUndoSnackbarDismissed() {
        _undoRemovedOverviewedPlaceSnackbackShown.value = null
    }

    fun resetSuggestedPlaces() {
        setSuggestedPlaces(null)
    }

    fun onSuggestedPlacesChanged() {
        _scrollSuggestedPlacesToTop.tryEmit(Unit)
    }

    fun onOverviewedPlacesChanged() {
        if (wasUndone) wasUndone = false
        else _scrollOverviewedPlacesToTop.tryEmit(Unit)
    }

    fun onSuggestedPlaceClicked(suggestedPlace: Place) {
        disableSuggestedPlaces()
        viewModelScope.onSuggestedPlaceClickedDelayed(suggestedPlace)
    }

    private fun CoroutineScope.onSuggestedPlaceClickedDelayed(suggestedPlace: Place) = launch {
        delay(350) // We manually wait for the ripple animation to finish
        if (!networkManager.hasInternet && !suggestedPlace.cached) {
            blinkInternetMissingBanner()
            enableSuggestedPlaces()
        } else emitOverviewedPlaces(suggestedPlace, overviewedPlacesSize + 1)
    }

    private fun enableSuggestedPlaces() {
        _suggestedPlacesEnabled.value = true
    }

    private fun disableSuggestedPlaces() {
        _suggestedPlacesEnabled.value = false
    }

    fun onOverviewedPlacesScrolled(verticalOffset: Int) {
        if (!wasVerticalDidiverHidden) isVerticalDividerShown = if (verticalOffset >= 1) {
            showView(_verticalDividerShown)
            true
        } else {
            hideView(_verticalDividerShown)
            false
        }
    }

    fun onOverviewedPlacesMoved(fromPosition: Int, toPosition: Int, isPortrait: Boolean) {
        if (isPortrait) {
            val placesToUpdate = mutableListOf<Place>()
            var topToBottom = false
            if (fromPosition < toPosition) {
                _overviewedPlaces.value?.forEachIndexed { index, place ->
                    if (index in fromPosition..toPosition) placesToUpdate.add(place)
                }
                topToBottom = true
            } else _overviewedPlaces.value?.forEachIndexed { index, place ->
                if (index in fromPosition downTo toPosition) placesToUpdate.add(place)
            }

            viewModelScope.swapOverviewedPlaces(placesToUpdate, topToBottom)
        } else {
            val fromPlace = _overviewedPlaces.value?.get(fromPosition)
            val toPlace = _overviewedPlaces.value?.get(toPosition)
            if (fromPlace != null && toPlace != null) {
                viewModelScope.swapOverviewedPlaces(fromPlace, toPlace)
            }
        }
    }

    fun onDraggingStarted() {
        _swipeToRefreshEnabled.value = false
    }

    fun onDraggingFinished() {
        _swipeToRefreshEnabled.value = true
    }

    fun onOverviewedPlacesSwiped(itemPosition: Int) {
        _overviewedPlaces.value?.get(itemPosition)?.let { place ->
            viewModelScope.removeOverviewedPlace(place) {
                removedOverviewedPlace = place
                _undoRemovedOverviewedPlaceSnackbackShown.value = place.info.place
            }
        }
    }

    private fun CoroutineScope.swapOverviewedPlaces(
        places: List<Place>,
        topToBottom: Boolean
    ) = launch(ioDispatcher) {
        repository.swapOverviewedPlaces(places, topToBottom)
    }

    private fun CoroutineScope.swapOverviewedPlaces(
        fromPlace: Place,
        toPlace: Place
    ) = launch(ioDispatcher) {
        repository.swapOverviewedPlaces(fromPlace, toPlace)
    }

    private fun CoroutineScope.undoRemovedOverviewedPlace(
        place: Place,
        onSuccess: () -> Unit
    ) = launch(ioDispatcher) {
        repository.addOverviewedPlace(place, onSuccess)
    }

    private fun CoroutineScope.removeOverviewedPlace(
        place: Place,
        onSuccess: () -> Unit
    ) = launch(ioDispatcher) {
        repository.removeOverviewedPlace(place, onSuccess)
    }

    private suspend fun emitOverviewedPlaces(
        place: Place,
        overviewedPlacesSize: Int
    ) = withContext(ioDispatcher) {
        repository.addOverviewedPlace(place, overviewedPlacesSize, networkManager.hasInternet)
    }

    private fun blinkInternetMissingBanner() {
        _blinkInternetMissingBanner.tryEmit(Unit)
    }

    private fun showSuggestedPlacesLoadingSpinner() {
        showView(_suggestedPlacesLoadingSpinnerShown)
    }

    private fun hideSuggestedPlacesLoadingSpinner() {
        hideView(_suggestedPlacesLoadingSpinnerShown)
    }

    private fun showLocationResetter() {
        _locationResetterVisibility.value = true
    }

    private fun hideLocationResetter() {
        _locationResetterVisibility.value = false
    }

    private fun clearLocationSearcherFocus() {
        _clearLocationSearcherFocus.tryEmit(Unit)
    }
}