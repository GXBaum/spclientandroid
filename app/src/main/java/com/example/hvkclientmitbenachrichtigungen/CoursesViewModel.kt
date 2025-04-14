package com.example.hvkclientmitbenachrichtigungen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hvkclientmitbenachrichtigungen.data.model.UserCourse
import com.example.hvkclientmitbenachrichtigungen.domaIn.repository.MyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val repository: MyRepository
): ViewModel() {
    // UI state
    private val _courses = MutableStateFlow<List<UserCourse>>(emptyList())
    val courses: StateFlow<List<UserCourse>> = _courses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Fetches courses for the given username
     */
    fun fetchCourses(username: String) {
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
