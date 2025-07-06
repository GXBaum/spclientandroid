package de.rafaelbeckmann.hvkclient.data.remote

import android.util.Log
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    @Volatile
    private var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        // Use a cached token if available, otherwise fetch it.
        val currentToken = token ?: runBlocking {
            settingsRepository.getAccessToken().also {
                token = it
            }
        }

        val request = if (currentToken != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $currentToken")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        Log.d("AuthInterceptor", "Response code: ${response.code}")

        // If the response is 401, the Authenticator will handle it.
        // We clear our cached token so the next request fetches the new one.
        if (response.code == 401 || response.code == 403) {
            token = null
        }

        return response
    }
}