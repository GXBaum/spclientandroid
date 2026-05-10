package de.rafaelbeckmann.hvkclient.ui.coursedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class CourseDetailScreenState(
    val marks: List<UserMark> = emptyList(),
    val courseName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
open class CourseDetailViewModel @Inject constructor(
    private val repository: HvkRepository,
    open val prefUtils: PrefUtils,
    private val settingsRepository: SettingsRepository
): ViewModel() {
    // UI state
    private val _courseDetailScreenState = MutableStateFlow(CourseDetailScreenState())
    val courseDetailScreenState: StateFlow<CourseDetailScreenState> = _courseDetailScreenState.asStateFlow()

    /**
     * Fetches courses for the given username
     */
    open fun fetchUserMarks(courseId: Int, userId: Int) {
        repository.getUserMarksForCourse(userId, courseId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _courseDetailScreenState.value = _courseDetailScreenState.value.copy(
                        isLoading = true,
                        marks = result.data ?: courseDetailScreenState.value.marks
                    )
                }
                is Resource.Success -> {
                    _courseDetailScreenState.value = _courseDetailScreenState.value.copy(
                        isLoading = false,
                        marks = result.data ?: courseDetailScreenState.value.marks,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _courseDetailScreenState.value = _courseDetailScreenState.value.copy(
                        isLoading = false,
                        error = result.message,
                        marks = result.data ?: courseDetailScreenState.value.marks
                    )
                }
            }
        }.launchIn(viewModelScope)
    }


    fun fetchCourseName(courseId: Int, userId: Int) {
        repository.getUserCourseById(userId, courseId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _courseDetailScreenState.value = _courseDetailScreenState.value.copy(
                        isLoading = true,
                        courseName = result.data?.name ?: courseDetailScreenState.value.courseName
                    )
                }
                is Resource.Success -> {
                    _courseDetailScreenState.value = _courseDetailScreenState.value.copy(
                        isLoading = false,
                        courseName = result.data?.name ?: courseDetailScreenState.value.courseName,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _courseDetailScreenState.value = _courseDetailScreenState.value.copy(
                        isLoading = false,
                        error = result.message,
                        courseName = result.data?.name ?: courseDetailScreenState.value.courseName
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    open suspend fun getUserId(): Int? {
        return settingsRepository.getUserId()
    }
}