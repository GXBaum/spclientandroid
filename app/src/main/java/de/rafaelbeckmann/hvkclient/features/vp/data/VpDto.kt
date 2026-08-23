package de.rafaelbeckmann.hvkclient.features.vp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class NetworkCourseSearchItem(
    val name: String
)

@Serializable
data class NetworkCourseSearchResponse(
    val courses: List<NetworkCourseSearchItem>
)


@Serializable
data class NetworkVpSelectedCourseRequest(
    val course: String
)

@Serializable
data class NetworkVpSelectedCoursesResponse(
    val courses: List<NetworkVpSelectedCourseResponse>
)

@Serializable
data class NetworkVpSelectedCourseResponse(
    val id: String,
    val course: String,
    val verified: Boolean
)

@Serializable
data class NetworkVpResponse(
    val substitutions: NetworkVpDays
)

@Serializable
data class NetworkVpDays(
    val today: NetworkVpDay?,
    val tomorrow: NetworkVpDay?
)

@Serializable
data class NetworkVpSubstitution(
    val id: Int = 0,
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String,
    val isDeleted: Boolean,
    @SerialName("VpType") val vpType: String,
    val courseName: String,

    val targetDate: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class NetworkVpInfoNeu(
    val id: Int,
    val text: String,
    val targetDate: String
)

@Serializable
data class NetworkVpDay(
    val substitutions: List<NetworkVpSubstitution> = emptyList(),
    val targetDate: String, // TODO: not a string
    val dayString: String,
    val info: List<NetworkVpInfoNeu>?
)