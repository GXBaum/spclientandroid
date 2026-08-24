package de.rafaelbeckmann.hvkclient.features.other.data

import kotlinx.serialization.Serializable

@Serializable
data class NetworkMigrateAccountDevV1Response(
    val token: String,
    val id: String
)

@Serializable
data class NetworkFeatureFlag(
    val featureFlags: Map<String, Boolean>
)

@Serializable
data class SpAuthCookieRequest(
    val authCookie: String,
    val cookies: List<NetworkCookie>? = null
)

@Serializable
data class NetworkCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val persistent: Boolean,
    val hostOnly: Boolean,
    val sameSite: String?,
)