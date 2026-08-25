package de.rafaelbeckmann.hvkclient.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rafaelbeckmann.hvkclient.core.domain.onError
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.features.other.domain.OtherStuffRepository
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
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