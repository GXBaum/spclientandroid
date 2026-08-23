package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult

interface AuthRepository {
    suspend fun createAccount(): EmptyResult<DataError>

    suspend fun login(username: String, password: String): EmptyResult<DataError>

    suspend fun addNotificationToken(token: String): EmptyResult<DataError>
}