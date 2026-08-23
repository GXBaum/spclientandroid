package de.rafaelbeckmann.hvkclient.features.vp.data

import de.rafaelbeckmann.hvkclient.features.vp.domain.SelectedCourse
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpDay
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpDays
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpInfoNeu
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpSubstitution
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
