package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.DataError
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.EmptyResult

interface AuthRepository {
    suspend fun createAccount(): EmptyResult<DataError>

    suspend fun login(username: String, password: String): EmptyResult<DataError>

    suspend fun addNotificationToken(token: String): EmptyResult<DataError>
}