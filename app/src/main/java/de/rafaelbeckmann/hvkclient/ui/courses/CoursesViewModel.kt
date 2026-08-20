package de.rafaelbeckmann.hvkclient.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.domain.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.repository.CoursesRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
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
    val prefUtils: PrefUtils,
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
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message ?: "Kurse konnten nicht aktualisiert werden"
                        )
                    }
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
