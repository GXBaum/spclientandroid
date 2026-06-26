package de.rafaelbeckmann.hvkclient.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
open class OnboardingViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val settingsRepository: SettingsRepository,
): ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        repository.login(username, password).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _loginState.value = LoginState.Loading
                }
                is Resource.Success -> {
                    result.data?.let { response ->
                        viewModelScope.launch {
                            settingsRepository.setAccessToken(response.accessToken)
                            settingsRepository.setRefreshToken(response.refreshToken)
                            settingsRepository.setUserId(response.userId)
                            settingsRepository.setOnboardingCompleted(true)

                            // TODO: Firebase.messaging.token.await() auch ins repo?
                            val fcmToken = Firebase.messaging.token.await()
                            val tokenUpdateRequest = NetworkTokenUpdateRequest(fcmToken)

                            repository.updateToken(tokenUpdateRequest)
                            _loginState.value = LoginState.Success
                        }
                    } ?: run {
                        _loginState.value = LoginState.Error("Login response was empty.")
                    }
                }
                is Resource.Error -> {
                    _loginState.value = LoginState.Error(result.message ?: "Ein unbekannter Fehler ist aufgetreten.")
                }
            }
        }.launchIn(viewModelScope)
    }



    fun createAccount() {
        repository.createAccount().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _loginState.value = LoginState.Loading
                }
                is Resource.Success -> {
                    result.data?.let { response ->
                        viewModelScope.launch {
                            settingsRepository.setAccessToken(response.token)
                            settingsRepository.setRefreshToken(response.refreshToken)
                            settingsRepository.setUserId(response.id)
                            settingsRepository.setOnboardingCompleted(true)

                            // TODO: Firebase.messaging.token.await() auch ins repo?
                            val fcmToken = Firebase.messaging.token.await()
                            val tokenUpdateRequest = NetworkTokenUpdateRequest(fcmToken)
                            repository.updateToken(tokenUpdateRequest)

                            _loginState.value = LoginState.Success
                        }
                    } ?: run {
                        _loginState.value = LoginState.Error("Create account response was empty.")
                    }
                }
                is Resource.Error -> {
                    _loginState.value = LoginState.Error(result.message ?: "Ein unbekannter Fehler ist aufgetreten.")
                }
            }
        }.launchIn(viewModelScope)
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}