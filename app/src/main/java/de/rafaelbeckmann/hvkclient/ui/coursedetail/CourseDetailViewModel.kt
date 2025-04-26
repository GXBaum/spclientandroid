package de.rafaelbeckmann.hvkclient.ui.coursedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.domain.repository.MyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class CourseDetailViewModel @Inject constructor(
    private val repository: MyRepository,
    open val prefUtils: PrefUtils
): ViewModel() {
    // UI state
    private val _marks = MutableStateFlow<List<UserMark>>(emptyList())
    open val marks: StateFlow<List<UserMark>> = _marks

    private val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    open val error: StateFlow<String?> = _error

    /**
     * Fetches courses for the given username
     */
    open fun fetchUserMarks(courseId: Int, username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getUserMarksForCourse(username, courseId)
                .catch { exception ->
                    _error.value = exception.message
                }
                .collect { result ->
                    _isLoading.value = false
                    result.fold(
                        onSuccess = { marks ->
                            _marks.value = marks
                        },
                        onFailure = { error ->
                            _error.value = error.message
                        }
                    )
                }
        }
    }
}