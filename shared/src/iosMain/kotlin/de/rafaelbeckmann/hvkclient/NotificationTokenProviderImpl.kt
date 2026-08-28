package de.rafaelbeckmann.hvkclient

import org.koin.core.annotation.Single

@Single(binds = [NotificationTokenProvider::class])
class NotificationTokenProviderImpl(
) : NotificationTokenProvider {

    // FIXME: PLACEHOLDER
    override suspend fun getToken(): String? {
        return null
    }

}