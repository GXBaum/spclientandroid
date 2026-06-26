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

fun NetworkVpDay.toEntity(): VpDayEntity {
    return VpDayEntity(
        targetDate = this.targetDate,
        dayString = this.dayString,
    )
}

fun VpDayEntity.toDomain(substitutions: List<VpSubstitutionEntity>, info: List<VpInfoNeu>): VpDay {
    return VpDay(
        substitutions = substitutions.map { it.toDomain() },
        targetDate = this.targetDate,
        dayString = this.dayString,
        info = info
    )
}


// TODO: this seems stupid
fun mapEntitiesToVpDays(
    vpDaysWithInfo: List<VpDayWithInfo>,
    subs: List<VpSubstitutionEntity>
): VpDays {
    val today = vpDaysWithInfo.firstOrNull()
    val tomorrow = vpDaysWithInfo.getOrNull(1)

    return VpDays(
        today = VpDay(
            substitutions = subs.map { it.toDomain() }.filter { it.targetDate == today?.day?.targetDate },
            targetDate = today?.day?.targetDate ?: "",
            dayString = today?.day?.dayString ?: "",
            info = today?.info?.map { it.toDomain() }
        ),
        tomorrow = VpDay(
            substitutions = subs.map { it.toDomain() }.filter { it.targetDate == tomorrow?.day?.targetDate },
            targetDate = tomorrow?.day?.targetDate ?: "",
            dayString = tomorrow?.day?.dayString ?: "",
            info = tomorrow?.info?.map { it.toDomain() }
        )
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
        targetDate = this.targetDate
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
        targetDate = this.targetDate
    )
}

fun NetworkVpInfoNeu.toEntity(): VpDayInfoItem {
    return VpDayInfoItem(
        id = this.id,
        targetDate = this.targetDate,
        info = this.text
    )
}

fun VpDayInfoItem.toDomain(): VpInfoNeu {
    return VpInfoNeu(
        id = this.id,
        text = this.info,
        targetDate = this.targetDate
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
        courseId = this.courseId,
        name = this.name
    )
}

fun UserCourseEntity.toDomain(): UserCourse {
    return UserCourse(
        courseId = this.courseId,
        name = this.name
    )
}
