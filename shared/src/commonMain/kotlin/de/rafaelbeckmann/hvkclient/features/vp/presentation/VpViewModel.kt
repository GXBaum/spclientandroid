package de.rafaelbeckmann.hvkclient.features.vp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import de.rafaelbeckmann.hvkclient.core.presentation.toUiText
import de.rafaelbeckmann.hvkclient.features.vp.domain.SelectedCourse
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpDays
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class VpScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCourses: List<SelectedCourse> = emptyList(),
    val substitutions: VpDays? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class VpViewModel(
    private val vpRepository: VpRepository
) : ViewModel() {
    private val _vpScreenState = MutableStateFlow(VpScreenState())
    val vpScreenState: StateFlow<VpScreenState> = _vpScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            _vpScreenState.update { it.copy(isLoading = true, error = null) }

            refreshAsync()
            observeSelectedCourses()

            // Observe the selected course and fetch substitutions when it's available
            vpScreenState.map { it.selectedCourses }
                .distinctUntilChanged()
                .onEach {
                    viewModelScope.launch {
                        updateRefreshError(fetchSubstitutions(it))
                    }
                }
                .flatMapLatest { courses ->
                    vpRepository.observeSubstitutions(courses.map { it.name })
                        .onEach { substitutions ->
                            _vpScreenState.update { it.copy(substitutions = substitutions) }
                        }
                }
                .launchIn(viewModelScope)
        }
    }

    fun observeSelectedCourses() {
        vpRepository.observeSelectedCourses()
            .onEach { courses ->
                _vpScreenState.update { it.copy(selectedCourses = courses) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun fetchSubstitutions(courseNames: List<SelectedCourse>): EmptyResult<DataError> {
        return vpRepository.refreshSubstitutions(courseNames.map { it.name })
    }

    private fun updateRefreshError(
        substitutionResult: EmptyResult<DataError>,
        enrollmentResult: EmptyResult<DataError>? = null
    ) {
        val enrollmentError = (enrollmentResult as? Result.Error)?.error
        val substitutionError = (substitutionResult as? Result.Error)?.error

        val error = enrollmentError ?: substitutionError
        _vpScreenState.update { it.copy(error = error?.toUiText()) }
    }

    suspend fun refresh() {
        _vpScreenState.update { it.copy(isLoading = true, error = null) }

        try {
            val enrollmentResult = vpRepository.refreshSelectedCourses()
            val substitutionResult = fetchSubstitutions(vpScreenState.value.selectedCourses)
            updateRefreshError(substitutionResult, enrollmentResult)
        } finally {
            _vpScreenState.update { it.copy(isLoading = false) }
        }
    }

    fun refreshAsync() {
        viewModelScope.launch { refresh() }
    }
}
