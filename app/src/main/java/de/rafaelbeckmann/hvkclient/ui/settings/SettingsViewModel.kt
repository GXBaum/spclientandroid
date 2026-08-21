package de.rafaelbeckmann.hvkclient.ui.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.UserPreferences
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.domain.model.SelectedCourse
import de.rafaelbeckmann.hvkclient.domain.repository.EncryptedUserPreferencesRepository
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SpRepositoryTest
import de.rafaelbeckmann.hvkclient.domain.repository.VpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Cookie
import javax.inject.Inject

data class SettingsScreenState(
    val vpSelectedCourse: List<SelectedCourse> = emptyList(),
    val courseSearch: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeveloper: Boolean = false,
    val useDynamicColor: Boolean? = null,
    val userId: String? = null,

    val spAuthTest: List<Cookie> = emptyList(),
    val encryptedUserPreferences: UserPreferences? = null
)

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val vpRepository: VpRepository,
    private val settingsRepository: SettingsRepository,
    open val prefUtils: PrefUtils,

    private val spTestRepository: SpRepositoryTest,
    private val encryptedUserPreferencesRepository: EncryptedUserPreferencesRepository
) : ViewModel() {

    private val _settingsScreenState = MutableStateFlow(SettingsScreenState())
    val settingsScreenState: StateFlow<SettingsScreenState> = _settingsScreenState.asStateFlow()

    private val _courseSearch = MutableStateFlow<List<String>>(emptyList())
    open val courseSearch: StateFlow<List<String>> = _courseSearch

    var isDeveloper = mutableStateOf(false)

    // TODO ich verstehe .stateIn nicht
    val useDynamicColor: StateFlow<Boolean?> = settingsRepository
        .useDynamicColorFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            isDeveloper.value = settingsRepository.isDeveloper()

            _settingsScreenState.value = _settingsScreenState.value.copy(
                isDeveloper = settingsRepository.isDeveloper(),
                userId = settingsRepository.getUserId()
            )

            Log.d("SettingsViewModel", "refreshToken: ${settingsRepository.getRefreshToken()}")

            fetchSelectedCourse()
            observeSelectedCourses()
        }
    }

    fun reload() {
        fetchSelectedCourse()
    }

    fun saveUsername(userId: String) {
        viewModelScope.launch {
            settingsRepository.setUserId(userId)
            fetchSelectedCourse()
        }
    }


    // TODO: fetcht mehrere Male
    fun fetchSelectedCourse() {
        viewModelScope.launch {
            _settingsScreenState.update {
                it.copy(isLoading = true, error = null)
            }

            vpRepository.refreshSelectedCourses()
                .onFailure { exception ->
                    _settingsScreenState.update {
                        it.copy(
                            error = exception.message ?: "Klassen konnten nicht aktualisiert werden"
                        )
                    }
                }

            _settingsScreenState.update { it.copy(isLoading = false) }
        }
    }

    fun observeSelectedCourses() {
        vpRepository.observeSelectedCourses()
            .onEach { value ->
                _settingsScreenState.update {
                    it.copy(
                        vpSelectedCourse = value
                    )
                }
            }
            .launchIn(viewModelScope)
    }




    fun postSelectedCourse(courseName: String) {
        if (courseName.isBlank()) return

        viewModelScope.launch {
            _settingsScreenState.value = _settingsScreenState.value.copy(
                isLoading = true,
                error = null,
            )

            vpRepository.addSelectedCourse(courseName)
                .onFailure { exception ->
                    _settingsScreenState.update {
                        it.copy(
                            error = exception.message ?: "Klasse konnte nicht hinzugefügt werden"
                        )
                    }
                }

            _settingsScreenState.update { it.copy(isLoading = false) }
        }
    }

    // TODO: irgendwie mehr responsive machen
    fun deleteVpSelectedCourse(courseId: String) {
        viewModelScope.launch {
            _settingsScreenState.value = _settingsScreenState.value.copy(
                isLoading = true,
                error = null,
            )

            vpRepository.removeSelectedCourse(courseId)
                .onFailure { exception ->
                    _settingsScreenState.update {
                        it.copy(
                            error = exception.message ?: "Klasse konnte nicht gelöscht werden"
                        )
                    }
                }

            _settingsScreenState.update { it.copy(isLoading = false) }
        }
    }

    // TODO: maybe don't search for every letter and cache?
    fun searchCourses(courseName: String) {
        repository.getCourseSearch(courseName).onEach { result ->
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





    fun getSpAuthCookieTest() {
        viewModelScope.launch {
            _settingsScreenState.value = _settingsScreenState.value.copy(
                spAuthTest = spTestRepository.getSpAuthCookiesTest()
            )
            Log.d("TEST", settingsScreenState.value.spAuthTest.toString())

            val formattedCookie = settingsScreenState.value.spAuthTest.map { cookie -> cookie.toString().split(";")[0] }.joinToString("; ")
            Log.d("TEST", formattedCookie)

            repository.postSpAuthCookie(
                settingsScreenState.value.spAuthTest
            )
        }
    }

    fun getSpTest() {
        viewModelScope.launch {
            repository.getSpTest()
        }
    }

    fun getEncryptedUserPreferences() {
        encryptedUserPreferencesRepository.getUserPreferences().onEach { result ->
            _settingsScreenState.value = _settingsScreenState.value.copy(
                encryptedUserPreferences = result
            )
        }.launchIn(viewModelScope)
    }

    fun setEncryptedUserPreferences(userPreferences: UserPreferences) {
        viewModelScope.launch {
            encryptedUserPreferencesRepository.setUserPreferences(userPreferences)
        }
    }
}
