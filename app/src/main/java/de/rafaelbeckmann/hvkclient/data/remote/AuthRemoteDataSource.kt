package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.core.data.safeCall
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCreateAccountResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkLoginResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkTokenUpdateRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun postAccount(): Result<NetworkCreateAccountResponse, DataError.Remote> {
        return safeCall {
            httpClient.post(
                "auth/register"
            ) {
            }
        }
    }

    // should this take in a LoginRequest class?
    suspend fun login(username: String, password: String): Result<NetworkLoginResponse, DataError.Remote> {
        return safeCall {
            httpClient.post(
                "auth/login"
            ) {
                setBody(NetworkLoginRequest(username, password))
            }
        }
    }

    suspend fun postFcmToken(token: String): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post(
                "users/me/notification-token"
            ) {
                setBody(NetworkTokenUpdateRequest(token))
            }
        }
    }

}