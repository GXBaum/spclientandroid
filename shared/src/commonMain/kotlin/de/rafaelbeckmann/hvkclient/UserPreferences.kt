package de.rafaelbeckmann.hvkclient

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val refreshToken: String? = null,
    val spUsername: String? = null,
    val spPassword: String? = null,
    val spSidCookie: String? = null,
    val spAesKeyTokenMessages: String? = null
)