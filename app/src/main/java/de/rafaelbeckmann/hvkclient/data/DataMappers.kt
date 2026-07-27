package de.rafaelbeckmann.hvkclient.data

import de.rafaelbeckmann.hvkclient.data.model.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserCourseEntity
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.UserMarkEntity
import de.rafaelbeckmann.hvkclient.data.model.VpDay
import de.rafaelbeckmann.hvkclient.data.model.VpDayEntity
import de.rafaelbeckmann.hvkclient.data.model.VpDayInfoItem
import de.rafaelbeckmann.hvkclient.data.model.VpDayWithInfo
import de.rafaelbeckmann.hvkclient.data.model.VpDays
import de.rafaelbeckmann.hvkclient.data.model.VpInfoNeu
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourseEntity
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionEntity
import de.rafaelbeckmann.hvkclient.data.model.VpType
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkFeatureFlag
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserCourse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserMark
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpDay
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpInfoNeu
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCourseResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSubstitution
import de.rafaelbeckmann.hvkclient.ui.settings.SelectedCourse
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun NetworkVpDay.toEntity(): VpDayEntity {
    return VpDayEntity(
        targetDate = Instant.parse(this.targetDate),
        dayString = this.dayString,
    )
}

fun VpDayEntity.toDomain(substitutions: List<VpSubstitutionEntity>, info: List<VpInfoNeu>): VpDay {
    return VpDay(
        substitutions = substitutions.map { it.toDomain() },
        targetDate = LocalDateTime.ofInstant(this.targetDate.toJavaInstant(), ZoneId.systemDefault()).toLocalDate(),
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

    val subsToday = subs.filter { sub ->
        val subDate = LocalDateTime.ofInstant(sub.targetDate.toJavaInstant(), ZoneId.systemDefault()).toLocalDate()
        val dayDate = LocalDateTime.ofInstant(testToday?.day?.targetDate?.toJavaInstant(), ZoneId.systemDefault()).toLocalDate()
        subDate == dayDate
    }.map { it.toDomain() }

    val substTomorrow = subs.filter { sub ->
        val subDate = LocalDateTime.ofInstant(sub.targetDate.toJavaInstant(), ZoneId.systemDefault()).toLocalDate()
        val dayDate = LocalDateTime.ofInstant(testTomorrow?.day?.targetDate?.toJavaInstant(), ZoneId.systemDefault()).toLocalDate()
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
        targetDate = LocalDateTime.ofInstant(this.targetDate.toJavaInstant(), ZoneId.systemDefault()).toLocalDate(), // LocalDate.ofInstant() has min API 34
        createdAt = LocalDateTime.ofInstant(this.createdAt.toJavaInstant(), ZoneId.systemDefault()),
        updatedAt = LocalDateTime.ofInstant(this.updatedAt.toJavaInstant(), ZoneId.systemDefault())
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
        targetDate = LocalDateTime.ofInstant(this.targetDate.toJavaInstant(), ZoneId.systemDefault()).toLocalDate().toString()
    )
}

fun VpDayWithInfo.toDomain(substitutions: List<VpSubstitution>): VpDay {
    return VpDay(
        substitutions = substitutions,
        targetDate = LocalDateTime.ofInstant(this.day.targetDate.toJavaInstant(), ZoneId.systemDefault()).toLocalDate(),
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
