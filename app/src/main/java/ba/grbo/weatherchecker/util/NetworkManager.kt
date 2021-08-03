package ba.grbo.weatherchecker.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED
import android.net.NetworkRequest
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetSocketAddress
import java.util.*

class NetworkManager(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val scope: CoroutineScope
) {
    private lateinit var networkCallback: NetworkCallback
    private lateinit var networkRequest: NetworkRequest

    private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableMap<Network, Boolean> =
        Collections.synchronizedMap(mutableMapOf())

    private lateinit var pinger: Job

    // Using SharedFlow instead of StateFlow, because SharedFlow waits for all collectors to
    // consume emitted value before emitting new values.
    private val _internetStatus = MutableSharedFlow<Boolean>(replay = 1)
    val internetStatus = _internetStatus.distinctUntilChanged()

    private var activated = false

    init {
        collectSubscriptionCount()
    }

    private fun collectSubscriptionCount() {
        scope.launch(ioDispatcher) {
            _internetStatus.subscriptionCount
                .map { count -> count > 0 }
                .distinctUntilChanged()
                .collectLatest { isActive ->
                    if (!activated && isActive) onActive()
                    else if (!isActive) {
                        delay(5000) // To avoid resetting on quick configuration changes
                        onInactive()
                    }
                }
        }
    }

    private fun onActive() {
        scope.emitInternetStatus()
        if (!::networkCallback.isInitialized) networkCallback = createNetworkCallback()
        if (!::networkRequest.isInitialized) networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
        pinger = scope.keepPinging()
        activated = true
    }

    private fun onInactive() {
        if (::networkCallback.isInitialized) {
            pinger.cancel()
            cm.unregisterNetworkCallback(networkCallback)
            validNetworks.clear()
            activated = false
        }
    }

    // An improvement would be to either make delay duration longer for metered networks or
    // not to ping with metered networks at all.
    private fun CoroutineScope.keepPinging() = launch(ioDispatcher) {
        while (isActive) this@NetworkManager.emitInternetStatusWithDelay(5000)
    }

    // Note, onAvailable is not called when cellular is turned on if the wifi was already on,
    // it is called immediately after wifi is turned off and its onLost is done, so we get
    // no internet status for a brief moment. Smooth transition implemented by introducing
    // a small delay before consuming values when collecting the stream.
    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val (hasInternetCapability, isNonMeteredCapability) = network.hasInternetAndIsNonMeteredCapabilities
            if (hasInternetCapability) {
                validNetworks[network] = isNonMeteredCapability
                scope.emitInternetStatus()
            }
        }

        override fun onLost(network: Network) {
            validNetworks.remove(network)
            scope.emitInternetStatus()
        }
    }

    private fun CoroutineScope.emitInternetStatus() = launch(ioDispatcher) {
        this@NetworkManager.emitInternetStatus()
    }

    private suspend fun emitInternetStatusWithDelay(duration: Long) {
        delay(duration)
        emitInternetStatus()
    }

    private suspend fun emitInternetStatus() = emitInternetStatus(
        when {
            validNetworks.isEmpty() -> false
            validNetworks.containsValue(true) -> {
                val nonMetered = validNetworks.filter { it.value }
                val metered: Map<Network, Boolean> by lazy { validNetworks.filter { !it.value } }
                doNetworksHaveInternet(nonMetered) || doNetworksHaveInternet(metered)
            }
            else -> doNetworksHaveInternet(validNetworks)
        }
    )

    private suspend fun emitInternetStatus(hasInternet: Boolean) = _internetStatus.emit(hasInternet)

    private fun doesNetworkHaveInternet(network: Network) = try {
        val socket = network.socketFactory.createSocket()
        socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
        socket.close()
        true
    } catch (e: Exception) {
        false
    }

    private suspend fun doNetworksHaveInternet(networks: Map<Network, Boolean>): Boolean {
        networks.forEach { (network, _) ->
            val hasInternet = doesNetworkHaveInternet(network)
            if (hasInternet) {
                emitInternetStatus(true)
                return true
            }
        }
        return false
    }

    private val Network.hasInternetAndIsNonMeteredCapabilities: Pair<Boolean, Boolean>
        get() {
            val nc = cm.getNetworkCapabilities(this)
            return Pair(
                nc?.hasCapability(NET_CAPABILITY_INTERNET) ?: false,
                nc?.hasCapability(NET_CAPABILITY_NOT_METERED) ?: false
            )
        }
}