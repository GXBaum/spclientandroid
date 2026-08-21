package de.rafaelbeckmann.hvkclient.ui.vp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.domain.model.SelectedCourse
import de.rafaelbeckmann.hvkclient.domain.model.VpDays
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.domain.repository.VpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
    private val vpRepository: VpRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _vpScreenState = MutableStateFlow(VpScreenState())
    open val vpScreenState: StateFlow<VpScreenState> = _vpScreenState.asStateFlow()


    // TODO: Wenn man aktualisiert, werden manchmal die alten Daten angezeigt, auch wenn es die nicht mehr gibt.
    init {
        viewModelScope.launch {
            _vpScreenState.value = _vpScreenState.value.copy(isLoading = true, error = null)

            //fetchVpSelectedCourse()
            refresh()
            observeSelectedCourses()


            // Observe the selected course and fetch substitutions when it's available
            vpScreenState.map { it.selectedCourses }
                .distinctUntilChanged()
                .onEach { courses ->
                    //fetchVpSubstitutions(courses)
                    refresh()
                    observeSubstitutions(courses)
                }.launchIn(viewModelScope)
        }
    }

    /*
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
    */

    fun observeSelectedCourses() {
        vpRepository.observeSelectedCourses()
            .onEach { value ->
                _vpScreenState.update {
                    it.copy(
                        selectedCourses = value
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun observeSubstitutions(courseNames: List<SelectedCourse>) {
        vpRepository.observeSubstitutions(courseNames.map { it.name })
            .onEach { value ->
                _vpScreenState.update {
                    it.copy(
                        substitutions = value
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun fetchSelectedCourse() {
        viewModelScope.launch {
            _vpScreenState.update {
                it.copy(isLoading = true, error = null)
            }

            vpRepository.refreshSelectedCourses()
                .onFailure { exception ->
                    _vpScreenState.update {
                        it.copy(
                            error = exception.message ?: "Klassen konnten nicht aktualisiert werden"
                        )
                    }
                }

            _vpScreenState.update { it.copy(isLoading = false) }
        }
    }

    fun fetchSubstitutions(courseNames: List<SelectedCourse>) {
        viewModelScope.launch {
            _vpScreenState.update {
                it.copy(isLoading = true, error = null)
            }

            vpRepository.refreshSubstitutions(courseNames.map { it.name })
                .onFailure { exception ->
                    _vpScreenState.update {
                        it.copy(
                            error = exception.message ?: "Klassen konnten nicht aktualisiert werden"
                        )
                    }
                }

            _vpScreenState.update { it.copy(isLoading = false) }
        }
    }

    fun refresh() {
        val courses = vpScreenState.value.selectedCourses
        //fetchVpSubstitutions(courses)

        fetchSelectedCourse()
        fetchSubstitutions(courses)
    }
}
