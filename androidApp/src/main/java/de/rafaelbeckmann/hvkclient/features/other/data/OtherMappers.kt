package de.rafaelbeckmann.hvkclient.features.other.data

import okhttp3.Cookie

fun NetworkFeatureFlag.toEntity(): List<FeatureFlagEntity> {
    return this.featureFlags.map {
        FeatureFlagEntity(
            key = it.key,
            value = it.value
        )
    }
}

fun Cookie.toDomain(): NetworkCookie {
    return NetworkCookie(
        name = this.name,
        value = this.value,
        expiresAt = this.expiresAt,
        domain = this.domain,
        path = this.path,
        secure = this.secure,
        httpOnly = this.httpOnly,
        persistent = this.persistent,
        hostOnly = this.hostOnly,
        sameSite = this.sameSite
    )
}