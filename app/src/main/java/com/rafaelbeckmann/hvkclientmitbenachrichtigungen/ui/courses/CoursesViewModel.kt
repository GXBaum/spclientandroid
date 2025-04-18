package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.PrefUtils
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserCourse
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.domaIn.repository.MyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class CoursesViewModel @Inject constructor(
    private val repository: MyRepository,
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
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getUserCourses(username)
                .catch { exception ->
                    _error.value = exception.message
                }
                .collect { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { courseList ->
                            _courses.value = courseList
                        },
                        onFailure = { error ->
                            _error.value = error.message
                        }
                    )
                }
        }
    }
}