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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
open class CoursesViewModel @Inject constructor(
    private val repository: HvkRepository,
    open val prefUtils: PrefUtils
): ViewModel() {
    // UI state
    private val _courses = MutableStateFlow<List<UserCourse>>(emptyList())
    open val courses: StateFlow<List<UserCourse>> = _courses

    private val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    open val error: StateFlow<String?> = _error

    /**
     * Fetches courses for the given username
     */
    open fun fetchCourses(username: String) {
        repository.getUserCourses(username).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                    _error.value = null
                    result.data?.let { _courses.value = it }
                }
                is Resource.Success -> {
                    _isLoading.value = false
                    _error.value = null
                    result.data?.let { _courses.value = it }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _error.value = result.message
                    result.data?.let { _courses.value = it }
                }
            }
        }.launchIn(viewModelScope)
    }
}