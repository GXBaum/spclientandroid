package de.rafaelbeckmann.hvkclient

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

@Single(binds = [ConnectivityObserver::class])
class NetworkConnectivityObserver(): ConnectivityObserver {

    // FIXME: PLACEHOLDER
    override val isConnected: Flow<Boolean> = flowOf(true)
}