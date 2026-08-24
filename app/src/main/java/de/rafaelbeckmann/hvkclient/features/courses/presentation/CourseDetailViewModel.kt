package de.rafaelbeckmann.hvkclient.features.courses.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.features.courses.domain.CoursesRepository
import de.rafaelbeckmann.hvkclient.features.courses.domain.UserMark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseDetailScreenState(
    val marks: List<UserMark> = emptyList(),
    val courseName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val coursesRepository: CoursesRepository,
    private val settingsRepository: SettingsRepository
): ViewModel() {
    // UI state
    private val _courseDetailScreenState = MutableStateFlow(CourseDetailScreenState())
    val courseDetailScreenState: StateFlow<CourseDetailScreenState> = _courseDetailScreenState.asStateFlow()

    fun observeMarks(courseId: Int) {
        coursesRepository.observeMarks(courseId)
            .onEach { value ->
                _courseDetailScreenState.update {
                    it.copy(
                        marks = value
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshMarks(courseId: Int) {
        viewModelScope.launch {
            _courseDetailScreenState.update {
                it.copy(isLoading = true, error = null)
            }

            coursesRepository.refreshMarks(courseId)
                .onError { error ->
                    _courseDetailScreenState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Noten konnten nicht geladen werden"
                            }
                        )
                    }
                }
                .onSuccess {
                    _courseDetailScreenState.update { it.copy(error = null) }
                }

            _courseDetailScreenState.update { it.copy(isLoading = false) }
        }
    }

    fun observeCourse(courseId: Int) {
        coursesRepository.observeCourse(courseId)
            .onEach { value ->
                _courseDetailScreenState.update {
                    it.copy(
                        courseName = value?.name
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    suspend fun getUserId(): String? {
        return settingsRepository.getUserId()
    }
}