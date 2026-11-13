package de.rafaelbeckmann.hvkclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.core.presentation.toUiText
import de.rafaelbeckmann.hvkclient.features.auth.domain.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class OnboardingViewModel(
    private val authRepository: AuthRepository,
    private val notificationTokenProvider: NotificationTokenProvider
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            authRepository.login(username, password)
                .onSuccess { completeSignIn() }
                .onError {
                    _loginState.value = LoginState.Error(it.toUiText("Ein Fehler ist aufgetreten. Versuche es später erneut."))
                }
        }
    }

    fun createAccount() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            authRepository.createAccount()
                .onSuccess { completeSignIn() }
                .onError {
                    _loginState.value = LoginState.Error(it.toUiText("Ein Fehler ist aufgetreten. Versuche es später erneut."))
                }
        }
    }

    private suspend fun completeSignIn() {
        try {
            val token = notificationTokenProvider.getToken()
            if (!token.isNullOrBlank()) {
                authRepository.addNotificationToken(token)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // Push registration is optional after successful authentication.
        }

        _loginState.value = LoginState.Success
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}