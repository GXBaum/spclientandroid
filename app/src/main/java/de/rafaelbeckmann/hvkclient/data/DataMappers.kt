package de.rafaelbeckmann.hvkclient.data

import de.rafaelbeckmann.hvkclient.data.local.entity.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.UserCourseEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.UserMarkEntity
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCookie
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkFeatureFlag
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserCourse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserMark
import de.rafaelbeckmann.hvkclient.domain.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.model.UserMark
import okhttp3.Cookie

fun NetworkUserMark.toEntity(): UserMarkEntity {
    return UserMarkEntity(
        id = this.id,
        name = this.name,
        date = this.date,
        grade = this.grade,
        course_id = this.courseId,
        half_year = this.halfYear,
        isDeleted = this.isDeleted
    )
}

fun UserMarkEntity.toDomain(): UserMark {
    return UserMark(
        id = this.id,
        name = this.name,
        date = this.date,
        grade = this.grade,
        courseId = this.course_id,
        halfYear = this.half_year,
        isDeleted = this.isDeleted
    )
}

fun NetworkFeatureFlag.toEntity(): List<FeatureFlagEntity> {
    return this.featureFlags.map {
        FeatureFlagEntity(
            key = it.key,
            value = it.value
        )
    }
}

fun NetworkUserCourse.toEntity(): UserCourseEntity {
    return UserCourseEntity(
        courseId = this.id,
        name = this.name
    )
}

fun UserCourseEntity.toDomain(): UserCourse {
    return UserCourse(
        id = this.courseId,
        name = this.name
    )
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
