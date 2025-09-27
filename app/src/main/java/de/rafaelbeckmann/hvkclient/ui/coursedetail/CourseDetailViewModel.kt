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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
open class CourseDetailViewModel @Inject constructor(
    private val repository: HvkRepository,
    open val prefUtils: PrefUtils,
    private val settingsRepository: SettingsRepository
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
    open fun fetchUserMarks(courseId: Int, userId: Int) {
        repository.getUserMarksForCourse(userId, courseId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _isLoading.value = true
                    result.data?.let {
                        _marks.value = it
                    }
                }
                is Resource.Success -> {
                    _isLoading.value = false
                    result.data?.let {
                        _marks.value = it
                    }
                    _error.value = null
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _error.value = result.message
                    result.data?.let {
                        _marks.value = it
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    open suspend fun getUserId(): Int? {
        return settingsRepository.getUserId()
    }
}