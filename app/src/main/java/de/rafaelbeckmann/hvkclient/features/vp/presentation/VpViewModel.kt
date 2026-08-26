package de.rafaelbeckmann.hvkclient.features.vp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.features.vp.domain.SelectedCourse
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpDays
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpRepository
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
                .onError { error ->
                    _vpScreenState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Klassen konnte nicht geladen werden"
                            }
                        )
                    }
                }
                .onSuccess {
                    _vpScreenState.update { it.copy(error = null) }
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
                .onError { error ->
                    _vpScreenState.update {
                        it.copy(
                            error = when (error) {
                                DataError.Remote.NO_INTERNET -> "kein Internet"
                                else -> "Einträge konnten nicht geladen werden"
                            }
                        )
                    }
                }
                .onSuccess {
                    _vpScreenState.update { it.copy(error = null) }
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
