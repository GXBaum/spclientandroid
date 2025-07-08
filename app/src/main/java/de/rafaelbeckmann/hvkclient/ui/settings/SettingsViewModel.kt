package de.rafaelbeckmann.hvkclient.ui.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val settingsRepository: SettingsRepository,
    open val prefUtils: PrefUtils
): ViewModel() {

    private val _vpSelectedCourse = MutableStateFlow<List<String>>(emptyList())
    open val vpSelectedCourse: StateFlow<List<String>> = _vpSelectedCourse

    private val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    open val error: StateFlow<String?> = _error

    var isDeveloper = mutableStateOf(false)
    var username = mutableStateOf("")

    init {
        viewModelScope.launch {
            isDeveloper.value = settingsRepository.isDeveloper()
            username.value = settingsRepository.getUsername() ?: ""

            // TODO: Remove this hardcoded token in production
            settingsRepository.setRefreshToken("38227ef324f32bda0ca8377a2059944aaffd2412ec1d32658eea5fffe66de2cd19f19064e7eb63bd1cec26a910833bb3ccc7df2f52fe0f9233b10b5f7927a7e2")
            Log.d("SettingsViewModel", "refreshToken: ${settingsRepository.getRefreshToken()}")

            if (username.value.isNotEmpty()) {
                fetchSpSelectedCourse(username.value)
            }
        }
    }

    fun saveUsername(username: String) {
        viewModelScope.launch {
            settingsRepository.setUsername(username)
        }
    }


    // TODO: fetcht mehrere Male
    fun fetchSpSelectedCourse(username: String) {
        repository.getVpSelectedCourses(username).onEach { result ->
            Log.d("SettingsViewModel", "username: $username")
            when (result) {
                is Resource.Loading -> {
                    Log.d("SettingsViewModel", "Loading vpSelectedCourse for user: $username - Result: $result")
                    _isLoading.value = true
                    result.data?.let {
                        _vpSelectedCourse.value = it
                    }
                }
                is Resource.Success -> {
                    Log.d("SettingsViewModel", "Success fetching vpSelectedCourse for user: $username - Data: ${result.data}")
                    _isLoading.value = false
                    _error.value = null
                    _vpSelectedCourse.value = result.data ?: emptyList()
                    Log.d("SettingsViewModel", "vpSelectedCourse: ${result.data}")
                }
                is Resource.Error -> {
                    Log.e("SettingsViewModel", "Error fetching vpSelectedCourse for user: $username, message: ${result.message}")
                    _isLoading.value = false
                    _error.value = result.message
                    result.data?.let {
                        _vpSelectedCourse.value = it
                    }
                }
            }
        }.catch { exception ->
            _isLoading.value = false
            _error.value = exception.message
        }.launchIn(viewModelScope)
    }




    // TODO: man kann einen Kurs "" erstellen, der dann nicht mehr gelöscht werden kann
    fun postVpSelectedCourse(courseName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            //settingsRepository.setVpSelectedCourseName(courseName)

            try {
                val courseObject = VpSelectedCourse(courseName)

                repository.postVpSelectedCourses(username.value, courseObject)

                // After posting successfully, refresh the data
                fetchSpSelectedCourse(username.value)
            } catch (exception: Exception) {
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    // TODO: irgendwie mehr responsive machen
    fun deleteVpSelectedCourse(courseName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.deleteVpSelectedCourse(username.value, courseName)

                // After deleting successfully, refresh the data
                fetchSpSelectedCourse(username.value)
            } catch (exception: Exception) {
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }


    fun toggleDeveloperMode(context: Context) {
        viewModelScope.launch {
            isDeveloper.value = !isDeveloper.value
            settingsRepository.setIsDeveloper(isDeveloper.value)

            if (isDeveloper.value) {
                Log.d("SettingsViewModel", "Developer mode enabled")

                Toast.makeText(
                    context,
                    "Du bist jetzt im Debug Modus (No Diddy)",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                Log.d("SettingsViewModel", "Developer mode disabled")

                Toast.makeText(
                    context,
                    "Du bist jetzt wieder im normalen Modus",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    fun resetOnboardingCompleted() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(false)
            Log.d("SettingsViewModel", "Onboarding completed reset")
        }
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            try {
                repository.clearCache()
                Toast.makeText(context, "Cache geleert", Toast.LENGTH_SHORT).show()
                Log.d("SettingsViewModel", "Cache cleared successfully")
            } catch (e: Exception) {
                Toast.makeText(context, "Fehler beim leeren des Caches", Toast.LENGTH_SHORT).show()
                Log.e("SettingsViewModel", "Failed to clear cache", e)
            }
        }
    }

    fun deleteAccessToken() {
        viewModelScope.launch {
            settingsRepository.setAccessToken("")
            Log.d("SettingsViewModel", "Access token deleted")
        }
    }
    fun deleteRefreshToken() {
        viewModelScope.launch {
            settingsRepository.setRefreshToken("")
            Log.d("SettingsViewModel", "Refresh token deleted")
        }
    }

}