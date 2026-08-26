package de.rafaelbeckmann.hvkclient.features.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class NetworkCreateAccountRequest(
    val placeholderCauseItWillErrorOtherwise: String? = null
)

@Serializable
data class NetworkCreateAccountResponse(
    val token: String,
    val refreshToken: String,
    val id: String
)

@Serializable
data class NetworkLoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class NetworkLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)

@Serializable
data class NetworkRefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class NetworkRefreshTokenResponse(
    val token: String
)

@Serializable
data class NetworkTokenUpdateRequest(
    val token: String
)
