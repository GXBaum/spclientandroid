package de.rafaelbeckmann.hvkclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
open class ConnectivityViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver
): ViewModel() {
    val isConnected = connectivityObserver
        .isConnected
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            true
        )

    // this is a deviation from the Tutorial (https://youtu.be/wvDPG2iQ-OE?si=PN5O5_AQEvyIVTTB)
    // tutorial uses SharingStarted.WhileSubscribed
}