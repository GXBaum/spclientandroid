package de.rafaelbeckmann.hvkclient.data.remote.dto

data class NetworkCreateAccountRequest(
    val placeholderCauseItWillErrorOtherwise: String? = null
)

data class NetworkCreateAccountResponse(
    val token: String,
    val refreshToken: String,
    val id: String
)

data class NetworkLoginRequest(
    val username: String,
    val password: String
)
data class NetworkLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String
)

data class NetworkRefreshTokenRequest(
    val refreshToken: String
)
data class NetworkRefreshTokenResponse(
    val token: String
)

data class NetworkTokenUpdateRequest(
    val token: String
)

data class NetworkMigrateAccountDevV1Response(
    val token: String,
    val id: String
)
