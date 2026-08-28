package de.rafaelbeckmann.hvkclient

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single

@Single(binds = [NotificationTokenProvider::class])
class NotificationTokenProviderImpl(
) : NotificationTokenProvider {
    override suspend fun getToken(): String {
        return Firebase.messaging.token.await()
    }

}