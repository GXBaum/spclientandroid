package de.rafaelbeckmann.hvkclient.features.other.data

import de.rafaelbeckmann.hvkclient.core.data.safeCall
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

@Single
class OtherStuffRemoteDataSource(
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