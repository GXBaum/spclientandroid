package de.rafaelbeckmann.hvkclient.data

import de.rafaelbeckmann.hvkclient.data.local.entity.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.UserCourseEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.UserMarkEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.VpDayEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.VpDayInfoItem
import de.rafaelbeckmann.hvkclient.data.local.entity.VpDayWithInfo
import de.rafaelbeckmann.hvkclient.data.local.entity.VpSelectedCourseEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.VpSubstitutionEntity
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkCookie
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkFeatureFlag
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserCourse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserMark
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpDay
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpInfoNeu
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCourseResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSubstitution
import de.rafaelbeckmann.hvkclient.domain.model.SelectedCourse
import de.rafaelbeckmann.hvkclient.domain.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.model.UserMark
import de.rafaelbeckmann.hvkclient.domain.model.VpDay
import de.rafaelbeckmann.hvkclient.domain.model.VpDays
import de.rafaelbeckmann.hvkclient.domain.model.VpInfoNeu
import de.rafaelbeckmann.hvkclient.domain.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.domain.model.VpType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okhttp3.Cookie
import kotlin.time.Instant

fun NetworkVpDay.toEntity(): VpDayEntity {
    return VpDayEntity(
        targetDate = Instant.parse(this.targetDate),
        dayString = this.dayString,
    )
}

fun VpDayEntity.toDomain(substitutions: List<VpSubstitutionEntity>, info: List<VpInfoNeu>): VpDay {
    return VpDay(
        substitutions = substitutions.map { it.toDomain() },
        targetDate = this.targetDate.toLocalDateTime(TimeZone.currentSystemDefault()).date,
        dayString = this.dayString,
        info = info
    )
}


// TODO: this seems stupid
fun mapEntitiesToVpDays(
    vpDaysWithInfo: List<VpDayWithInfo>,
    subs: List<VpSubstitutionEntity>
): VpDays {
    val testToday = vpDaysWithInfo.firstOrNull()
    val testTomorrow = vpDaysWithInfo.getOrNull(1)

    // TODO: crashes when only one day exists
    val subsToday = subs.filter { sub ->
        val subDate = sub.targetDate
        val dayDate = testToday?.day?.targetDate
        subDate == dayDate
    }.map { it.toDomain() }

    val substTomorrow = subs.filter { sub ->
        val subDate = sub.targetDate
        val dayDate = testTomorrow?.day?.targetDate
        subDate == dayDate
    }.map { it.toDomain() }

    val today = vpDaysWithInfo.firstOrNull()?.toDomain(subsToday)
    val tomorrow = vpDaysWithInfo.getOrNull(1)?.toDomain(substTomorrow)

    return VpDays(
        today = today,
        tomorrow = tomorrow
    )
}

fun NetworkVpSubstitution.toEntity(): VpSubstitutionEntity {
    return VpSubstitutionEntity(
        id = this.id,
        hour = this.hour,
        original = this.original,
        replacement = this.replacement,
        description = this.description,
        isDeleted = this.isDeleted,
        VpType = VpType.valueOf(this.vpType),
        courseName = this.courseName,
        targetDate = Instant.parse(this.targetDate),
        createdAt = Instant.parse(this.createdAt),
        updatedAt = Instant.parse(this.updatedAt)
    )
}

fun VpSubstitutionEntity.toDomain(): VpSubstitution {
    return VpSubstitution(
        id = this.id,
        hour = this.hour,
        original = this.original,
        replacement = this.replacement,
        description = this.description,
        isDeleted = this.isDeleted,
        VpType = this.VpType,
        courseName = this.courseName,
        targetDate = this.targetDate.toLocalDateTime(TimeZone.currentSystemDefault()).date,
        createdAt = this.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = this.updatedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    )
}

fun NetworkVpInfoNeu.toEntity(): VpDayInfoItem {
    return VpDayInfoItem(
        id = this.id,
        targetDate = Instant.parse(this.targetDate),
        info = this.text
    )
}

fun VpDayInfoItem.toDomain(): VpInfoNeu {
    return VpInfoNeu(
        id = this.id,
        text = this.info,
        targetDate = this.targetDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    )
}

fun VpDayWithInfo.toDomain(substitutions: List<VpSubstitution>): VpDay {
    return VpDay(
        substitutions = substitutions,
        targetDate = this.day.targetDate.toLocalDateTime(TimeZone.currentSystemDefault()).date,
        dayString = this.day.dayString,
        info = this.info.map { it.toDomain() }
    )
}

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

fun NetworkVpSelectedCourseResponse.toEntity(): VpSelectedCourseEntity {
    return VpSelectedCourseEntity(
        id = this.id,
        courseName = this.course,
        verified = this.verified
    )
}

fun VpSelectedCourseEntity.toDomain(): SelectedCourse {
    return SelectedCourse(
        id = this.id,
        name = this.courseName,
        verified = this.verified
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
