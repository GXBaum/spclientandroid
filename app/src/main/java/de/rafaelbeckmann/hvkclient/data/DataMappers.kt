package de.rafaelbeckmann.hvkclient.data

import de.rafaelbeckmann.hvkclient.data.local.entity.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCookie
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkFeatureFlag
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
