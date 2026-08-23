package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCookie
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkFeatureFlag
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkMigrateAccountDevV1Response
import de.rafaelbeckmann.hvkclient.data.remote.dto.SpAuthCookieRequest
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.DataError
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.EmptyResult
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.Result
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class OtherStuffRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getFeatureFlags(): Result<NetworkFeatureFlag, DataError.Remote> {
        return safeCall {
            httpClient.get(
                "featureFlags"
            ) {
            }
        }
    }

    suspend fun postSpAuthCookie(authCookie: String, cookies: List<NetworkCookie>? = null): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.post(
                "sp/authCookie"
            ) {
                setBody(
                    SpAuthCookieRequest(authCookie, cookies)
                )
            }
        }
    }

    suspend fun getSpTest(): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.get(
                "sp/test"
            ) {
            }
        }
    }

    suspend fun getDevV1Migration(userId: Number, refreshToken: String): Result<NetworkMigrateAccountDevV1Response, DataError.Remote> {
        return safeCall {
            httpClient.get(
                "migrations/dev-v1/$userId"
            ) {
                parameter("refreshTokenInRequest", refreshToken)
            }
        }
    }
}