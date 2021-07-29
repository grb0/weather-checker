package ba.grbo.weatherchecker.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ba.grbo.weatherchecker.di.IODispatcher
import ba.grbo.weatherchecker.util.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class WeatherCheckerViewModel @Inject constructor(
    @ApplicationContext
    context: Context, // safe as no ViewModel can outlive the application itself
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val internetStatus = NetworkManager(context, ioDispatcher, viewModelScope).internetStatus
}