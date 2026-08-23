package de.rafaelbeckmann.hvkclient.data.remote.dto

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