package de.rafaelbeckmann.hvkclient.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CoursesUiState(
    val courses: List<UserCourse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
open class CoursesViewModel @Inject constructor(
    private val repository: HvkRepository,
    open val prefUtils: PrefUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoursesUiState())
    open val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    /**
     * Fetches courses for the given username
     */
    open fun fetchCourses(username: String) {
        repository.getUserCourses(username).onEach { result ->
            _uiState.update { currentState ->
                when (result) {
                    is Resource.Loading -> {
                        currentState.copy(
                            isLoading = true,
                            error = null,
                            courses = result.data ?: currentState.courses
                        )
                    }
                    is Resource.Success -> {
                        currentState.copy(
                            isLoading = false,
                            error = null,
                            courses = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        currentState.copy(
                            isLoading = false,
                            error = result.message,
                            courses = result.data ?: currentState.courses
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
}