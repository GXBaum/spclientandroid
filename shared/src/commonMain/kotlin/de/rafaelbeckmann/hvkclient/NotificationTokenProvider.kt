package de.rafaelbeckmann.hvkclient

interface NotificationTokenProvider {
    suspend fun getToken(): String // should probably be nullable
}