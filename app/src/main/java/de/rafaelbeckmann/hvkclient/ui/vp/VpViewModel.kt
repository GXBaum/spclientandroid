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
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class VpViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val prefUtils: PrefUtils,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    open val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _vpSelectedCourse = MutableStateFlow<List<String>>(emptyList())
    open val vpSelectedCourse: StateFlow<List<String>> = _vpSelectedCourse

    val vpSubstitutionsAll = mutableStateOf<VpSubstitutionsAll?>(null)

    var username = mutableStateOf("")


    // TODO: Wenn man aktualisiert, werden manchmal die alten Daten angezeigt, auch wenn es die nicht mehr gibt.
    init {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            username.value = settingsRepository.getUsername() ?: ""

            if (username.value.isNotEmpty()) {
                fetchSpSelectedCourse(username.value)
            } else {
                _isLoading.value = false
                _error.value = "Username not set. Please set it in the settings."
            }

            // Observe the selected course and fetch substitutions when it's available
            vpSelectedCourse
                .onEach { courses ->
                    fetchVpSubstitutionsAll(courses)
                }.launchIn(viewModelScope)
        }
    }

    fun fetchSpSelectedCourse(username: String) {
        repository.getVpSelectedCourses(username).onEach { result ->
            Log.d("SettingsViewModel", "username: $username")
            when (result) {
                is Resource.Loading -> {
                    Log.d("SettingsViewModel", "Loading vpSelectedCourse for user: $username - Result: $result")
                    _isLoading.value = true
                    result.data?.let {
                        _vpSelectedCourse.value = it
                    }
                }

                is Resource.Success -> {
                    Log.d("SettingsViewModel", "Success fetching vpSelectedCourse for user: $username - Data: ${result.data}")
                    _isLoading.value = false
                    _error.value = null
                    _vpSelectedCourse.value = result.data ?: emptyList()
                    Log.d("SettingsViewModel", "vpSelectedCourse: ${result.data}")
                }
                is Resource.Error -> {
                    Log.e("SettingsViewModel", "Error fetching vpSelectedCourse for user: $username, message: ${result.message}")
                    _isLoading.value = false
                    _error.value = result.message
                    result.data?.let {
                        _vpSelectedCourse.value = it
                    }
                }
            }
        }.catch { exception ->
            _isLoading.value = false
            _error.value = exception.message
        }.launchIn(viewModelScope)
    }

    // TODO: api hinzufügen, die mehrere Kurse gleichzeitig abfragen kann
    fun fetchVpSubstitutionsAll(courseNames: List<String>) {
        val courseName = courseNames.firstOrNull()
        if (courseName.isNullOrEmpty()) {
            _isLoading.value = false
            vpSubstitutionsAll.value = VpSubstitutionsAll(emptyList())
            return
        }

        repository.getVpSubstitutionsAll(courseName)
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