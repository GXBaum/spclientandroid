package de.rafaelbeckmann.hvkclient.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
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
                    result.data?.let {
                        viewModelScope.launch {
                            settingsRepository.setAccessToken(it.accessToken)
                            settingsRepository.setRefreshToken(it.refreshToken)
                            settingsRepository.setUsername(username)
                            settingsRepository.setOnboardingCompleted(true)
                            _loginState.value = LoginState.Success
                        }

                        // TODO: Firebase.messaging.token.await() auch ins repo?
                        val tokenUpdateRequest = TokenUpdateRequest(Firebase.messaging.token.await(), username)
                        repository.updateToken(username, tokenUpdateRequest)
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
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}