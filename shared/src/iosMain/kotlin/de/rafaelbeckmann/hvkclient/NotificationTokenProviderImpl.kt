package de.rafaelbeckmann.hvkclient

import de.rafaelbeckmann.hvkclient.features.auth.domain.AuthRepository
import org.koin.core.annotation.Single

@Single(binds = [NotificationTokenProvider::class, NotificationTokenProviderImpl::class])
class NotificationTokenProviderImpl(
    private val authRepository: AuthRepository
) : NotificationTokenProvider {

    private var currentToken: String? = null
    // FIXME: PLACEHOLDER
    override suspend fun getToken(): String? {
        return currentToken
    }

    suspend fun onNewFcmToken(token: String) {
        currentToken = token
        authRepository.addNotificationToken(token)
    }
}