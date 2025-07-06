package de.rafaelbeckmann.hvkclient.data.remote

import android.util.Log
import de.rafaelbeckmann.hvkclient.data.model.RefreshTokenRequest
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val api: HvkClientApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TokenAuthenticator", "Authenticating request: ${response.request.url}")
        Log.d("TokenAuthenticator", "Response code: ${response.code}")
        if (response.code == 401 || response.code == 403) {
            return runBlocking {
                val refreshToken = settingsRepository.getRefreshToken() ?: return@runBlocking null

                val refreshResponse = api.refreshToken(RefreshTokenRequest(refreshToken))

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val newAccessToken = refreshResponse.body()!!.accessToken
                    settingsRepository.setAccessToken(newAccessToken)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    // If refresh fails, we can't recover, so we don't retry.
                    null
                }
            }
        }
        return null
    }
}