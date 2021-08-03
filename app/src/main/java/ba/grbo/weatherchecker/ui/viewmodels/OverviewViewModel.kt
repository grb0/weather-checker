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
import ba.grbo.weatherchecker.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val repository: Repository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    networkManager: NetworkManager
) : ViewModel() {
    private val internetStatus = networkManager.internetStatus

    private var _userInitializedUnfocus = false
    val userInitializedUnfocus: Boolean
        get() = _userInitializedUnfocus

    private val location = MutableStateFlow<String?>(null)

    private val locationWithInternetStatus = location.combine(internetStatus) { l, i -> l to i }

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

    private val _suggestedPlacesCardShown = MutableStateFlow(false)
    val suggestedPlacesCardShown: StateFlow<Boolean>
        get() = _suggestedPlacesCardShown

    private val _suggestedPlacesShown = MutableStateFlow(false)
    val suggestedPlacesCShown: StateFlow<Boolean>
        get() = _suggestedPlacesShown

    private val _loadingSpinnerShown = MutableStateFlow(false)
    val loadingSpinnerShown: StateFlow<Boolean>
        get() = _loadingSpinnerShown

    private val _exceptionSnackbarShown = MutableStateFlow(false)
    val exceptionSnackbarShown: StateFlow<Boolean>
        get() = _exceptionSnackbarShown

    private val _scrollSuggestedPlacesToTop = SingleSharedFlow<Unit>()
    val scrollSuggestedPlacesToTop: SharedFlow<Unit>
        get() = _scrollSuggestedPlacesToTop

    init {
        // viewModelScope.collectLatestLocation()
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
                    if (loc.length < 3) repository.updateSuggestedPlaces(null)
                    else if (!_suggestedPlacesCardShown.value) showLoadingSpinner()
                    delay(SEARCHER_DEBOUNCE_PERIOD)
                    if (loc.length >= 3) repository.updateSuggestedPlaces(loc, hasInternet)
                } else {
                    hideLocationResetter()
                    repository.updateSuggestedPlaces(null)
                }
            }
        }
    }

    private fun CoroutineScope.collectLatestSuggestedPlaces() = launch(ioDispatcher) {
        repository.suggestedPlaces.collectLatest { suggestedPlaces ->
            when (suggestedPlaces) {
                null -> hideAndUpdateSuggestedPlaces()
                is Success -> if (suggestedPlaces.data.isNotEmpty()) {
                    showAndUpdateSuggestedPlaces(suggestedPlaces.data)
                } else hideAndUpdateSuggestedPlaces()
                // is Success -> showAndUpdateSuggestedPlaces(suggestedPlaces.data)
                is Error -> notifyUserOfError(suggestedPlaces.exception)
                is Loading -> showLoadingSpinner()
            }
        }
    }

    private fun hideAndUpdateSuggestedPlaces() {
        if (_suggestedPlacesCardShown.value) hideSuggestedPlacesCard()
        if (_suggestedPlacesShown.value) hideSuggestedPlaces()
        hideLoadingSpinner()
    }

    private fun showAndUpdateSuggestedPlaces(suggestedPlaces: List<Place>) {
        if (!_suggestedPlacesCardShown.value) showSuggestedPlacesCard()
        if (!_suggestedPlacesShown.value) showSuggestedPlaces()
        hideLoadingSpinner()
        updateSuggestedPlaces(suggestedPlaces)
    }

    private fun updateSuggestedPlaces(suggestedPlaces: List<Place>?) {
        _suggestedPlaces.value = suggestedPlaces
    }

    private fun showSuggestedPlacesCard() {
        _suggestedPlacesCardShown.value = true
    }

    private fun hideSuggestedPlacesCard() {
        _suggestedPlacesCardShown.value = false
    }

    private fun showSuggestedPlaces() {
        _suggestedPlacesShown.value = true
    }

    private fun hideSuggestedPlaces() {
        _suggestedPlacesShown.value = false
    }

    // According to the exception caught we can show an appropriate message
    // For simplicity we're gonna show the same message
    private fun notifyUserOfError(exception: Exception) {
        hideAndUpdateSuggestedPlaces()
        _exceptionSnackbarShown.value = true
    }

    fun onSnackbarMessageAcknowledge() {
        _exceptionSnackbarShown.value = false
    }

    fun resetSuggestedPlaces() {
        updateSuggestedPlaces(null)
    }

    fun onSuggestedPlacesChanged() {
        _scrollSuggestedPlacesToTop.tryEmit(Unit)
    }

    private fun showLoadingSpinner() {
        if (!_suggestedPlacesCardShown.value) showSuggestedPlacesCard()
        _loadingSpinnerShown.value = true
    }

    private fun hideLoadingSpinner() {
        _loadingSpinnerShown.value = false
    }

    private fun showLocationResetter() {
        _locationResetterVisibility.value = true
    }

    private fun hideLocationResetter() {
        _locationResetterVisibility.value = false
    }
}