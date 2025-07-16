package de.rafaelbeckmann.hvkclient.ui.vp

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.VpResponse
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VpScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCourses: List<String> = emptyList(),
    val substitutions: VpResponse? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
open class VpViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    var username = mutableStateOf("")

    private val _vpScreenState = MutableStateFlow(VpScreenState())
    open val vpScreenState: StateFlow<VpScreenState> = _vpScreenState.asStateFlow()


    // TODO: Wenn man aktualisiert, werden manchmal die alten Daten angezeigt, auch wenn es die nicht mehr gibt.
    init {
        viewModelScope.launch {
            _vpScreenState.value = _vpScreenState.value.copy(isLoading = true, error = null)
            username.value = settingsRepository.getUsername() ?: ""

            if (username.value.isNotEmpty()) {
                fetchSpSelectedCourse(username.value)
            } else {
                _vpScreenState.value = _vpScreenState.value.copy(isLoading = false, error = "Username not set. Please set it in the settings.")
            }

            // Observe the selected course and fetch substitutions when it's available
            vpScreenState.map { it.selectedCourses }
                .distinctUntilChanged()
                .onEach { courses ->
                    if (courses.isNotEmpty()) {
                        fetchVpSubstitutionsMultipleCourses(courses)
                    }
                }.launchIn(viewModelScope)
        }
    }

    fun fetchSpSelectedCourse(username: String) {
        repository.getVpSelectedCourses(username).onEach { result ->
            Log.d("SettingsViewModel", "username: $username")
            when (result) {
                is Resource.Loading -> {
                    Log.d("SettingsViewModel", "Loading vpSelectedCourse for user: $username - Result: $result")
                    _vpScreenState.value = _vpScreenState.value.copy(isLoading = true)
                    result.data?.let {
                        _vpScreenState.value = _vpScreenState.value.copy(selectedCourses = it)
                    }
                }

                is Resource.Success -> {
                    Log.d("SettingsViewModel", "Success fetching vpSelectedCourse for user: $username - Data: ${result.data}")
                    _vpScreenState.value = _vpScreenState.value.copy(
                        isLoading = false,
                        error = null,
                        selectedCourses = result.data ?: emptyList()
                    )
                    Log.d("SettingsViewModel", "vpSelectedCourse: ${result.data}")
                }
                is Resource.Error -> {
                    Log.e("SettingsViewModel", "Error fetching vpSelectedCourse for user: $username, message: ${result.message}")
                    _vpScreenState.value = _vpScreenState.value.copy(isLoading = false, error = result.message)
                    result.data?.let {
                        _vpScreenState.value = _vpScreenState.value.copy(selectedCourses = it)
                    }
                }
            }
        }.catch { exception ->
            _vpScreenState.value = _vpScreenState.value.copy(isLoading = false, error = exception.message)
        }.launchIn(viewModelScope)
    }

    fun fetchVpSubstitutionsMultipleCourses(courseNames: List<String>) {
        if (courseNames.isEmpty()) {
            _vpScreenState.value = _vpScreenState.value.copy(isLoading = false, substitutions = null)
            return
        }

        repository.getVpSubstitutionsMultipleCourses(courseNames).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _vpScreenState.value = _vpScreenState.value.copy(isLoading = true)
                    result.data?.let {
                        Log.d("VpViewModel", "Substitutions ALL loading: ${result.data}")

                        _vpScreenState.value = _vpScreenState.value.copy(substitutions = it)
                    }
                }
                is Resource.Success -> {
                    result.data?.let {
                        Log.d("VpViewModel", "Substitutions ALL success: ${result.data}")

                        _vpScreenState.value = _vpScreenState.value.copy(
                            isLoading = false,
                            error = null,
                            substitutions = it
                        )
                    }
                    Log.d("VpViewModel", "Substitutions ALL: ${result.data}")
                }
                is Resource.Error -> {
                    result.data?.let {
                        Log.d("VpViewModel", "Substitutions ALL error: ${result.data}")

                        _vpScreenState.value = _vpScreenState.value.copy(
                            isLoading = false,
                            error = null,
                            substitutions = it
                        )
                    }
                    Log.e("VpViewModel", "Error fetching substitutions: ${result.message}")
                }
            }
        }.launchIn(viewModelScope)

    }
}