package de.rafaelbeckmann.hvkclient.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.UserPreferences
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.domain.repository.EncryptedUserPreferencesRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SpRepositoryTest
import de.rafaelbeckmann.hvkclient.features.other.data.NetworkCookie
import de.rafaelbeckmann.hvkclient.features.other.domain.OtherStuffRepository
import de.rafaelbeckmann.hvkclient.features.vp.domain.SelectedCourse
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsScreenState(
    val vpSelectedCourse: List<SelectedCourse> = emptyList(),
    val courseSearch: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeveloper: Boolean = false,
    val useDynamicColor: Boolean? = null,
    val userId: String? = null,

    val spAuthTest: List<NetworkCookie> = emptyList(),
    val encryptedUserPreferences: UserPreferences? = null
)

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val vpRepository: VpRepository,
    private val otherRepository: OtherStuffRepository,
    private val settingsRepository: SettingsRepository,
    private val spTestRepository: SpRepositoryTest,
    private val encryptedUserPreferencesRepository: EncryptedUserPreferencesRepository
) : ViewModel() {

    private val _settingsScreenState = MutableStateFlow(SettingsScreenState())
    val settingsScreenState: StateFlow<SettingsScreenState> = _settingsScreenState.asStateFlow()

    private val _courseSearch = MutableStateFlow<List<String>>(emptyList())
    open val courseSearch: StateFlow<List<String>> = _courseSearch

    // TODO ich verstehe .stateIn nicht
    val useDynamicColor: StateFlow<Boolean?> = settingsRepository
        .useDynamicColorFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            val isDev = settingsRepository.isDeveloper()
            val userId = settingsRepository.getUserId()

            _settingsScreenState.update {
                it.copy(
                    isDeveloper = isDev,
                    userId = userId
                )
            }

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
                .onError { error ->
                    _settingsScreenState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Klassen konnten nicht geladen werden"
                            }
                        )
                    }
                }
                .onSuccess {
                    _settingsScreenState.update { it.copy(error = null) }
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
            _settingsScreenState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            vpRepository.addSelectedCourse(courseName)
                .onError { error ->
                    _settingsScreenState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Klasse konnte nicht hinzugefügt werden"
                            }
                        )
                    }
                }
                .onSuccess {
                    _settingsScreenState.update { it.copy(error = null) }
                }

            _settingsScreenState.update { it.copy(isLoading = false) }
        }
    }

    // TODO: irgendwie mehr responsive machen
    fun deleteVpSelectedCourse(courseId: String) {
        viewModelScope.launch {
            _settingsScreenState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }

            vpRepository.removeSelectedCourse(courseId)
                .onError { error ->
                    _settingsScreenState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Klasse konnte nicht gelöscht werden"
                            }
                        )
                    }
                }
                .onSuccess {
                    _settingsScreenState.update { it.copy(error = null) }
                }

            _settingsScreenState.update { it.copy(isLoading = false) }
        }
    }

    // TODO: maybe don't search for every letter and cache?
    fun searchCourses(courseName: String) {
        viewModelScope.launch {
            _settingsScreenState.update {
                it.copy(isLoading = true, error = null)
            }

            vpRepository.searchCourses(courseName)
                .onError { error ->
                    _settingsScreenState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Klassensuche fehlgeschlagen"
                            }
                        )
                    }
                }
                .onSuccess { courses ->
                    _courseSearch.value = courses
                    _settingsScreenState.update {
                        it.copy(
                            error = null,
                            courseSearch = courses
                        )
                    }
                }

            _settingsScreenState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleDeveloperMode() {
        viewModelScope.launch {
            val newDevMode = !_settingsScreenState.value.isDeveloper

            _settingsScreenState.update {
                it.copy(isDeveloper = newDevMode)
            }

            settingsRepository.setIsDeveloper(newDevMode)
        }
    }

    fun resetOnboardingCompleted() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(false)
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                otherRepository.clearCache()
            } catch (e: Exception) {
            }
        }
    }

    fun deleteAccessToken() {
        viewModelScope.launch {
            settingsRepository.setAccessToken("")
        }
    }

    fun deleteRefreshToken() {
        viewModelScope.launch {
            settingsRepository.setRefreshToken("")
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

            otherRepository.postSpAuthCookie(
                settingsScreenState.value.spAuthTest
            )
        }
    }

    fun getSpTest() {
        viewModelScope.launch {
            otherRepository.getSpTest()
        }
    }

    fun getEncryptedUserPreferences() {
        encryptedUserPreferencesRepository.getUserPreferences().onEach { result ->
            _settingsScreenState.update {
                it.copy(encryptedUserPreferences = result)
            }
        }.launchIn(viewModelScope)
    }

    fun setEncryptedUserPreferences(userPreferences: UserPreferences) {
        viewModelScope.launch {
            encryptedUserPreferencesRepository.setUserPreferences(userPreferences)
        }
    }
}
