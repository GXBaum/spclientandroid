package de.rafaelbeckmann.hvkclient.ui.vp

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsAll
import de.rafaelbeckmann.hvkclient.domain.repository.MyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.net.URLEncoder
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

    val vpSubstitutionsAll = mutableStateOf(emptyList<VpSubstitutionsAll>())


    init {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            vpSelectedCourseName.value = prefUtils.getString("vpSelectedCourseName") ?: ""

            fetchVpSubstitutionsAll(vpSelectedCourseName.value)
        }

    }

    fun fetchVpSubstitutionsAll(courseName: String) {
        viewModelScope.launch {
            Log.d("VpViewModel", "Fetching all substitutions for course: $courseName")

            // URL encoding
            val encodedCourseName = URLEncoder.encode(courseName, "UTF-8")

            _isLoading.value = true
            _error.value = null

            repository.getVpSubstitutionsAll(encodedCourseName)
                .catch { exception ->
                    _error.value = exception.message
                }
                .collect { result ->
                    _isLoading.value = false
                    Log.d("VpViewModel", "Result ALL: $result")
                    result.fold(
                        onSuccess = { substitutions ->
                            Log.d("VpViewModel", "Substitutions ALL: ${substitutions}")
                            vpSubstitutionsAll.value = listOf(substitutions)
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