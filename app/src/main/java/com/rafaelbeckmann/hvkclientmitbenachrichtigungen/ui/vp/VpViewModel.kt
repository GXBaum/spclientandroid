package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.vp

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.PrefUtils
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.VpSubstitution
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.domaIn.repository.MyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpViewModel @Inject constructor(
    private val repository: MyRepository,
    private val prefUtils: PrefUtils
): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val vpSelectedCourseName = mutableStateOf("")

    val vpSubstitutions = mutableStateOf(emptyList<VpSubstitution>())


    init {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            vpSelectedCourseName.value = prefUtils.getString("vpSelectedCourseName") ?: ""

            fetchVpSubstitutions(vpSelectedCourseName.value)
        }

    }

    fun fetchVpSubstitutions(courseName: String) {
        viewModelScope.launch {
            Log.d("VpViewModel", "Fetching substitutions for course: $courseName")

            // URL encoding
            val encodedCourseName = java.net.URLEncoder.encode(courseName, "UTF-8")

            _isLoading.value = true
            _error.value = null

            repository.getVpSubstitutions(encodedCourseName)
                .catch { exception ->
                    _error.value = exception.message
                }
                .collect { result ->
                    _isLoading.value = false
                    Log.d("VpViewModel", "Result: $result")
                    result.fold(
                        onSuccess = { substitutions ->
                            Log.d("VpViewModel", "Substitutions: ${substitutions}")
                            vpSubstitutions.value = substitutions.map { it }
                        },
                        onFailure = { exception ->
                            Log.e("VpViewModel", "Error fetching substitutions: ${exception.message}")
                            _error.value = exception.message
                        }
                    )
                }
        }
    }
}