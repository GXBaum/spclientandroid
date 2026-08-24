package de.rafaelbeckmann.hvkclient.features.courses.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.features.courses.domain.CoursesRepository
import de.rafaelbeckmann.hvkclient.features.courses.domain.UserCourse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoursesUiState(
    val courses: List<UserCourse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val coursesRepository: CoursesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    init {
        observeCourses()
    }

    fun refresh() {
        refreshCourses()
    }

     private fun observeCourses() {
        coursesRepository.observeCourses()
            .onEach { value ->
                _uiState.update {
                    it.copy(courses = value)
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshCourses() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            coursesRepository.refreshCourses()
                .onError { error ->
                    _uiState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Kurse konnten nicht geladen werden"
                            }
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(error = null) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun getUserId(): String? {
        return settingsRepository.getUserId()
    }

    suspend fun isDeveloper(): Boolean {
        return settingsRepository.isDeveloper()
    }
}
