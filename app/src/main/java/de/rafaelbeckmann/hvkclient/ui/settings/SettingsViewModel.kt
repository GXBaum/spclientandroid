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
) : ViewModel() {

    private val _vpSelectedCourse = MutableStateFlow<List<String>>(emptyList())
    open val vpSelectedCourse: StateFlow<List<String>> = _vpSelectedCourse

    private val _courseSearch = MutableStateFlow<List<String>>(emptyList())
    open val courseSearch: StateFlow<List<String>> = _courseSearch

    private val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    open val error: StateFlow<String?> = _error

    var isDeveloper = mutableStateOf(false)
    var userId = mutableStateOf<Int?>(null)

    init {
        viewModelScope.launch {
            isDeveloper.value = settingsRepository.isDeveloper()
            userId.value = settingsRepository.getUserId()

            Log.d("SettingsViewModel", "refreshToken: ${settingsRepository.getRefreshToken()}")

            if (userId.value != null) {
                fetchSpSelectedCourse(userId.value!!)
            }
        }
    }

    fun saveUsername(userId: Int) {
        viewModelScope.launch {
            settingsRepository.setUserId(userId)
            fetchSpSelectedCourse(userId)
        }
    }


    // TODO: fetcht mehrere Male
    fun fetchSpSelectedCourse(userId: Int) {
        repository.getVpSelectedCourses(userId).onEach { result ->
            Log.d("SettingsViewModel", "username: ${this@SettingsViewModel.userId}")
            when (result) {
                is Resource.Loading -> {
                    Log.d("SettingsViewModel", "Loading vpSelectedCourse for user: ${this@SettingsViewModel.userId} - Result: $result")
                    _isLoading.value = true
                    result.data?.let {
                        _vpSelectedCourse.value = it
                    }
                }
                is Resource.Success -> {
                    Log.d("SettingsViewModel", "Success fetching vpSelectedCourse for user: ${this@SettingsViewModel.userId} - Data: ${result.data}")
                    _isLoading.value = false
                    _error.value = null
                    _vpSelectedCourse.value = result.data ?: emptyList()
                    Log.d("SettingsViewModel", "vpSelectedCourse: ${result.data}")
                }
                is Resource.Error -> {
                    Log.e("SettingsViewModel", "Error fetching vpSelectedCourse for user: ${this@SettingsViewModel.userId}, message: ${result.message}")
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
        if (courseName.isBlank()) return
        userId.value?.let { id ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null

                //settingsRepository.setVpSelectedCourseName(courseName)

                try {
                    val courseObject = VpSelectedCourse(courseName)

                    repository.postVpSelectedCourses(id, courseObject)

                    // After posting successfully, refresh the data
                    fetchSpSelectedCourse(id)
                } catch (exception: Exception) {
                    _error.value = exception.message
                    _isLoading.value = false
                }
            }
        }
    }

    // TODO: irgendwie mehr responsive machen
    fun deleteVpSelectedCourse(courseName: String) {
        userId.value?.let { id ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null

                try {
                    repository.deleteVpSelectedCourse(id, courseName)

                    // After deleting successfully, refresh the data
                    fetchSpSelectedCourse(id)
                } catch (exception: Exception) {
                    _error.value = exception.message
                    _isLoading.value = false
                }
            }
        }
    }

    // TODO: maybe don't search for every letter and cache?
    fun searchCourses(courseName: String) {
        repository.getCourseSearch(courseName).onEach { result ->
            Log.d("SettingsViewModel", "username: $userId")
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                    result.data?.let {
                        _courseSearch.value = it
                    }
                }
                is Resource.Success -> {
                    _isLoading.value = false
                    _error.value = null
                    _courseSearch.value = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _error.value = result.message
                    result.data?.let {
                        _courseSearch.value = it
                    }
                }
            }
        }.catch { exception ->
            _isLoading.value = false
            _error.value = exception.message
        }.launchIn(viewModelScope)
    }


    fun toggleDeveloperMode(context: Context) {
        viewModelScope.launch {
            isDeveloper.value = !isDeveloper.value
            settingsRepository.setIsDeveloper(isDeveloper.value)
            Toast.makeText(
                context,
                if (isDeveloper.value) "Du bist jetzt im Debug Modus (No Diddy)" else "Du bist jetzt wieder im normalen Modus",
                Toast.LENGTH_LONG
            ).show()
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
