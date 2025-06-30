package de.rafaelbeckmann.hvkclient.ui.vp

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsAll
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
open class VpViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val prefUtils: PrefUtils
): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val vpSelectedCourseName = mutableStateOf("")

    val vpSubstitutionsAll = mutableStateOf<VpSubstitutionsAll?>(null)


    init {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            vpSelectedCourseName.value = prefUtils.getString("vpSelectedCourseName") ?: ""

            if (vpSelectedCourseName.value.isNotEmpty()) {
                fetchVpSubstitutionsAll(vpSelectedCourseName.value)
            } else {
                _isLoading.value = false
            }
        }

    }

    fun fetchVpSubstitutionsAll(courseName: String) {
        // URL encoding
        val encodedCourseName = URLEncoder.encode(courseName, "UTF-8")

        repository.getVpSubstitutionsAll(encodedCourseName)
            .onEach { result ->
                Log.d("VpViewModel", "Result ALL: $result")
                when (result) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                        result.data?.let {
                            vpSubstitutionsAll.value = it
                        }
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _error.value = null
                        vpSubstitutionsAll.value = result.data
                        Log.d("VpViewModel", "Substitutions ALL: ${result.data}")
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _error.value = result.message
                        result.data?.let {
                            vpSubstitutionsAll.value = it
                        }
                        Log.e("VpViewModel", "Error fetching substitutions: ${result.message}")
                    }
                }
            }
            .catch { exception ->
                _isLoading.value = false
                _error.value = exception.message
            }
            .launchIn(viewModelScope)
    }
}