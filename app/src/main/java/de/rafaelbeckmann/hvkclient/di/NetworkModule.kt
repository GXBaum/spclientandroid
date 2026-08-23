package de.rafaelbeckmann.hvkclient.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkRefreshTokenRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkRefreshTokenResponse
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://hvk-api.rafaelbeckmann.de/v1/"

    @Provides
    @Singleton
    fun provideHttpClient(
        settingsRepository: SettingsRepository
    ): HttpClient {
        return HttpClient(OkHttp) {
            defaultRequest {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.ANDROID
            }
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(Auth) {
                bearer {
                    //sendWithoutRequest { true } // TODO: not sure if I need this, i think it works without it, Agent wants it though
                    loadTokens {
                        val accessToken = settingsRepository.getAccessToken() ?: return@loadTokens null
                        val refreshToken = settingsRepository.getRefreshToken()

                        BearerTokens(accessToken, refreshToken)
                    }
                    refreshTokens {
                        val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null

                        val response = client.post(
                            "auth/refresh"
                        ) {
                            setBody(
                                NetworkRefreshTokenRequest(
                                    refreshToken
                                )
                            )
                        }

                        if (response.status == HttpStatusCode.OK) {
                            val data: NetworkRefreshTokenResponse = response.body()

                            settingsRepository.setAccessToken(data.token)

                            BearerTokens(data.token, refreshToken)
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }
}