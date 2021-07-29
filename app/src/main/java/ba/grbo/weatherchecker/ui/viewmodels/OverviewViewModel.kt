package ba.grbo.weatherchecker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.weatherchecker.util.Constants.SEARCHER_DEBOUNCE_PERIOD
import ba.grbo.weatherchecker.util.SingleSharedFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor() : ViewModel() {
    private var _userInitializedUnfocus = false
    val userInitializedUnfocus: Boolean
        get() = _userInitializedUnfocus

    private val _location = MutableStateFlow<String?>(null)

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

    init {
        viewModelScope.collectLatestLocation()
    }

    fun onLocationSearcherFocusChanged(hasFocus: Boolean) {
        _onLocationSearcherFocusChanged.value = hasFocus
    }

    fun onLocationSearcherTextChanged(location: String) {
        this._location.value = location
    }

    fun onLocationResetterClicked() {
        _resetLocationSearcherText.tryEmit(Unit)
    }

    fun onScreenTouched(isLocationSearcherTouched: Boolean) {
        if (!isLocationSearcherTouched) requestLocationSearcherUnfocus()
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

    private fun CoroutineScope.collectLatestLocation() = launch(Dispatchers.Default) {
        _location.collectLatest {
            it?.let {
                if (it.isNotEmpty()) {
                    showLocationResetter()
                    // Other function to be invoked later, show autocomplete from db
                    delay(SEARCHER_DEBOUNCE_PERIOD)
                    // Other function to be invoked later, if not in db, resolve using geocoding
                } else hideLocationResetter()
            }
        }
    }

    private fun showLocationResetter() {
        _locationResetterVisibility.value = true
    }

    private fun hideLocationResetter() {
        _locationResetterVisibility.value = false
    }
}