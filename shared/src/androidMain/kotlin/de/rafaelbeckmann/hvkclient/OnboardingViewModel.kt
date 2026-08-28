package de.rafaelbeckmann.hvkclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.features.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class OnboardingViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            authRepository.login(username, password)
                .onSuccess {
                    // should this be in here?
                    // also i think this can crash lol. but its at least not worse than before, just the same ¯\_(ツ)_/¯
                    val fcmToken = Firebase.messaging.token.await()
                    authRepository.addNotificationToken(fcmToken)
                        .onSuccess {
                            _loginState.value = LoginState.Success
                        }
                        .onError {
                            // TODO: no error handling
                            _loginState.value = LoginState.Success
                        }
                }
                .onError {
                    _loginState.value = LoginState.Error("Ein Fehler ist aufgetreten. $it")
                }
        }
    }

    fun createAccount() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            authRepository.createAccount()
                .onSuccess {
                    // TODO: Firebase.messaging.token.await() auch ins repo?
                    val fcmToken = Firebase.messaging.token.await()
                    authRepository.addNotificationToken(fcmToken)
                        .onSuccess {
                            _loginState.value = LoginState.Success
                        }
                        .onError {
                            // TODO: no error handling
                            _loginState.value = LoginState.Success
                        }
                }
                .onError {
                    _loginState.value = LoginState.Error("Ein Fehler ist aufgetreten. $it")
                }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}