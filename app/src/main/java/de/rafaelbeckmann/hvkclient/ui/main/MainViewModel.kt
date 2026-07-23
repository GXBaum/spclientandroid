package de.rafaelbeckmann.hvkclient.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val settingsRepository: SettingsRepository,
    val prefUtils: PrefUtils
) : ViewModel() {

    fun migrateDevV1(userId: Number, refreshToken: String) {
        repository.devV1Migration(userId, refreshToken).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    //_loginState.value = LoginState.Loading
                }
                is Resource.Success -> {
                    result.data?.let { response ->
                        viewModelScope.launch {
                            settingsRepository.setAccessToken(response.token)
                            settingsRepository.setUserId(response.id)
                            settingsRepository.setOnboardingCompleted(true)

                            // TODO: Firebase.messaging.token.await() auch ins repo?
                            val fcmToken = Firebase.messaging.token.await()
                            val tokenUpdateRequest = NetworkTokenUpdateRequest(fcmToken)
                            repository.updateToken(tokenUpdateRequest)

                            //_loginState.value = LoginState.Success
                        }
                    } ?: run {
                        //_loginState.value = LoginState.Error("Create account response was empty.")
                    }
                }
                is Resource.Error -> {
                    //_loginState.value = LoginState.Error(result.message ?: "Ein unbekannter Fehler ist aufgetreten.")
                }
            }
        }.launchIn(viewModelScope)
    }
}