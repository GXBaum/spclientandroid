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
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourseRequest
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: should potentially not exist, as its redundant with other class
data class SelectedCourse(
    val name: String,
    val verified: Boolean
)

data class SettingsScreenState(
    val vpSelectedCourse: List<SelectedCourse> = emptyList(),
    val courseSearch: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeveloper: Boolean = false,
    val useDynamicColor: Boolean? = null,
    val userId: Int? = null
)

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val settingsRepository: SettingsRepository,
    open val prefUtils: PrefUtils
) : ViewModel() {

    private val _settingsScreenState = MutableStateFlow(SettingsScreenState())
    val settingsScreenState: StateFlow<SettingsScreenState> = _settingsScreenState.asStateFlow()

    private val _courseSearch = MutableStateFlow<List<String>>(emptyList())
    open val courseSearch: StateFlow<List<String>> = _courseSearch

    var isDeveloper = mutableStateOf(false)
    var userId = mutableStateOf<Int?>(null)

    // TODO ich verstehe .stateIn nicht
    val useDynamicColor: StateFlow<Boolean?> = settingsRepository
        .useDynamicColorFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            isDeveloper.value = settingsRepository.isDeveloper()
            userId.value = settingsRepository.getUserId()

            _settingsScreenState.value = _settingsScreenState.value.copy(
                isDeveloper = settingsRepository.isDeveloper(),
                userId = settingsRepository.getUserId()
            )

            Log.d("SettingsViewModel", "refreshToken: ${settingsRepository.getRefreshToken()}")

            if (userId.value != null) {
                fetchVpSelectedCourse(userId.value!!)
            }
        }
    }

    fun reload() {
        userId.value?.let{
            fetchVpSelectedCourse(it)
        }
    }

    fun saveUsername(userId: Int) {
        viewModelScope.launch {
            settingsRepository.setUserId(userId)
            fetchVpSelectedCourse(userId)
        }
    }


    // TODO: fetcht mehrere Male
    fun fetchVpSelectedCourse(userId: Int) {
        repository.getVpSelectedCourses(userId).onEach { result ->
            Log.d("SettingsViewModel", "username: ${this@SettingsViewModel.userId}")
            when (result) {
                is Resource.Loading -> {
                    Log.d("SettingsViewModel", "Loading vpSelectedCourse for user: ${this@SettingsViewModel.userId} - Result: $result")

                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = true,
                        vpSelectedCourse = result.data ?: settingsScreenState.value.vpSelectedCourse
                    )
                }
                is Resource.Success -> {
                    Log.d("SettingsViewModel", "Success fetching vpSelectedCourse for user: ${this@SettingsViewModel.userId} - Data: ${result.data}")

                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = false,
                        error = null,
                        vpSelectedCourse = result.data ?: emptyList()
                    )

                    Log.d("SettingsViewModel", "vpSelectedCourse: ${result.data}")
                }
                is Resource.Error -> {
                    Log.e("SettingsViewModel", "Error fetching vpSelectedCourse for user: ${this@SettingsViewModel.userId}, message: ${result.message}")

                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = false,
                        error = result.message,
                        vpSelectedCourse = result.data ?: settingsScreenState.value.vpSelectedCourse
                    )
                }
            }
        }.catch { exception ->
            _settingsScreenState.value = _settingsScreenState.value.copy(
                isLoading = false,
                error = exception.message
            )
        }.launchIn(viewModelScope)
    }




    // TODO: man kann einen Kurs "" erstellen, der dann nicht mehr gelöscht werden kann
    fun postVpSelectedCourse(courseName: String) {
        if (courseName.isBlank()) return
        userId.value?.let { id ->
            viewModelScope.launch {
                _settingsScreenState.value = _settingsScreenState.value.copy(
                    isLoading = true,
                    error = null,
                )

                //settingsRepository.setVpSelectedCourseName(courseName)

                try {
                    val courseObject = VpSelectedCourseRequest(courseName)

                    repository.postVpSelectedCourses(id, courseObject)

                    // After posting successfully, refresh the data
                    fetchVpSelectedCourse(id)
                } catch (exception: Exception) {
                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = false,
                        error = exception.message,
                    )
                }
            }
        }
    }

    // TODO: irgendwie mehr responsive machen
    fun deleteVpSelectedCourse(courseName: String) {
        userId.value?.let { id ->
            viewModelScope.launch {
                _settingsScreenState.value = _settingsScreenState.value.copy(
                    isLoading = true,
                    error = null,
                )

                try {
                    repository.deleteVpSelectedCourse(id, courseName)

                    // After deleting successfully, refresh the data
                    fetchVpSelectedCourse(id)
                } catch (exception: Exception) {
                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = false,
                        error = exception.message,
                    )
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
                    result.data?.let {
                        _courseSearch.value = it
                    }

                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = true,
                        courseSearch = result.data ?: settingsScreenState.value.courseSearch
                    )
                }
                is Resource.Success -> {
                    _courseSearch.value = result.data ?: emptyList()

                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = false,
                        error = null,
                        courseSearch = result.data ?: emptyList()
                    )
                }
                is Resource.Error -> {
                    result.data?.let {
                        _courseSearch.value = it
                    }

                    _settingsScreenState.value = _settingsScreenState.value.copy(
                        isLoading = false,
                        error = result.message,
                        courseSearch = result.data ?: settingsScreenState.value.courseSearch
                    )
                }
            }
        }.catch { exception ->
            _settingsScreenState.value = _settingsScreenState.value.copy(
                isLoading = false,
                error = exception.message,
            )
        }.launchIn(viewModelScope)
    }


    fun toggleDeveloperMode(context: Context) {
        viewModelScope.launch {
            isDeveloper.value = !isDeveloper.value

            _settingsScreenState.value = _settingsScreenState.value.copy(
                isDeveloper = !settingsScreenState.value.isDeveloper
            )

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

    fun toggleDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDynamicColor(enabled)
        }
    }

}
