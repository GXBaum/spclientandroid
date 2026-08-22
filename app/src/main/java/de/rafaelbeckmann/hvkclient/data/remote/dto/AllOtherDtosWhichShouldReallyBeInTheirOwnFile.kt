package de.rafaelbeckmann.hvkclient.data.remote.dto

import com.squareup.moshi.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkUserMarks(
    val marks: List<NetworkUserMark>
)

@Serializable
data class NetworkUserMark(
    val id: Int,
    val name: String,
    val date: String,
    val grade: String,
    val courseId: Int,
    val halfYear: Int,
    val isDeleted: Boolean
)

@Serializable
data class NetworkCourseSearchItem(
    val name: String
)

@Serializable
data class NetworkCourseSearchResponse(
    val courses: List<NetworkCourseSearchItem>
)

@Serializable // has serializable because it has custom decoder
data class NetworkUserCourse(
    val id: Int,
    val name: String
)

@Serializable
data class NetworkSingleCourseResponse(
    val course: NetworkUserCourse
)

@Serializable
data class NetworkUserCoursesResponse(
    val courses: String // custom decoder for encryption in the future, but still have to decide
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
    @SerialName("VpType") @param:Json(name = "VpType") val vpType: String,
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

@Serializable
data class NetworkFeatureFlag(
    val featureFlags: Map<String, Boolean>
)

@Serializable
data class SpAuthCookieRequest(
    val authCookie: String,
    val cookies: List<NetworkCookie>? = null
)

@Serializable
data class NetworkCookie(
    val name: String,
    val value: String,
    val expiresAt: Long,
    val domain: String,
    val path: String,
    val secure: Boolean,
    val httpOnly: Boolean,
    val persistent: Boolean,
    val hostOnly: Boolean,
    val sameSite: String?,
)