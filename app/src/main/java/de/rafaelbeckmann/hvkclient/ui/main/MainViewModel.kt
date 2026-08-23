package de.rafaelbeckmann.hvkclient.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.domain.repository.OtherStuffRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val otherRepository: OtherStuffRepository
) : ViewModel() {
    fun migrateDevV1(userId: Number, refreshToken: String) {

        viewModelScope.launch {
            otherRepository.devV1Migration(userId, refreshToken)
                .onSuccess {

                }
                .onError {

                }
        }
    }
}