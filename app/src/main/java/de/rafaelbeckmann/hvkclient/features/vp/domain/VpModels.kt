package de.rafaelbeckmann.hvkclient.features.vp.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

enum class VpType {
    substitution, differentRoom
}

data class VpSubstitution(
    val id: Int = 0,
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String,
    val isDeleted: Boolean,
    val VpType: VpType,
    val courseName: String,

    val targetDate: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class VpInfoNeu(
    val id: Int,
    val text: String,
    val targetDate: String
)

data class VpDay(
    val substitutions: List<VpSubstitution> = emptyList(),
    val targetDate: LocalDate,
    val dayString: String,
    val info: List<VpInfoNeu>?
)

data class VpDays(
    val today: VpDay?,
    val tomorrow: VpDay?
)

data class SelectedCourse(
    val id: String,
    val name: String,
    val verified: Boolean
)