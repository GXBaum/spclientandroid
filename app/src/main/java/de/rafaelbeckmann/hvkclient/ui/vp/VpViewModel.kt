package de.rafaelbeckmann.hvkclient.ui.vp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.VpDays
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.ui.settings.SelectedCourse
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
    val selectedCourses: List<SelectedCourse> = emptyList(),
    val substitutions: VpDays? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
open class VpViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _vpScreenState = MutableStateFlow(VpScreenState())
    open val vpScreenState: StateFlow<VpScreenState> = _vpScreenState.asStateFlow()


    // TODO: Wenn man aktualisiert, werden manchmal die alten Daten angezeigt, auch wenn es die nicht mehr gibt.
    init {
        viewModelScope.launch {
            _vpScreenState.value = _vpScreenState.value.copy(isLoading = true, error = null)

            fetchVpSelectedCourse()


            // Observe the selected course and fetch substitutions when it's available
            vpScreenState.map { it.selectedCourses }
                .distinctUntilChanged()
                .onEach { courses ->
                    fetchVpSubstitutions(courses)
                }.launchIn(viewModelScope)
        }
    }

    fun fetchVpSelectedCourse() {
        repository.getVpSelectedCourses().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    Log.d("VpViewModel", "Loading vpSelectedCourse - Result: $result")
                    _vpScreenState.value = _vpScreenState.value.copy(isLoading = true)
                    result.data?.let {
                        _vpScreenState.value = _vpScreenState.value.copy(selectedCourses = it)
                    }
                }
                is Resource.Success -> {
                    Log.d("VpViewModel", "Success fetching vpSelectedCourse - Data: ${result.data}")
                    _vpScreenState.value = _vpScreenState.value.copy(
                        isLoading = false,
                        error = null,
                        selectedCourses = result.data ?: emptyList()
                    )
                    Log.d("VpViewModel", "vpSelectedCourse: ${result.data}")
                }
                is Resource.Error -> {
                    Log.e("VpViewModel", "Error fetching vpSelectedCourse, message: ${result.message}")
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

    fun fetchVpSubstitutions(courses: List<SelectedCourse>) {
        if (courses.isEmpty()) {
            _vpScreenState.value = _vpScreenState.value.copy(isLoading = false, substitutions = null)
            return
        }

        repository.getVpSubstitutions(courses.map { it.name }).onEach { result ->
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
    fun refresh() {
        val courses = vpScreenState.value.selectedCourses
        fetchVpSubstitutions(courses)
    }
}
