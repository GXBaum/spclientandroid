package de.rafaelbeckmann.hvkclient.data.remote.dto

import com.squareup.moshi.Json


data class NetworkUserMarks(
    val marks: List<NetworkUserMark>
)
data class NetworkUserMark(
    val id: Int,
    val name: String,
    val date: String,
    val grade: String,
    @param:Json(name = "course_id") val courseId: Int,
    @param:Json(name = "half_year") val halfYear: Int,
    val isDeleted: Boolean
)

data class NetworkCourseSearchItem(
    val name: String
)
data class NetworkCourseSearchResponse(
    val courses: List<NetworkCourseSearchItem>
)

data class NetworkUserCourse(
    val courseId: Int,
    val name: String
)

data class NetworkSingleCourseResponse(
    val course: NetworkUserCourse
)

data class NetworkUserCoursesResponse(
    val courses: List<NetworkUserCourse>
)

data class NetworkVpSelectedCourseRequest(
    val course: String
)

data class NetworkVpSelectedCoursesResponse(
    val courses: List<NetworkVpSelectedCourseResponse>
)
data class NetworkVpSelectedCourseResponse(
    val id: String,
    val course: String,
    val verified: Boolean
)

data class NetworkVpResponse(
    val substitutions: NetworkVpDays
)


data class NetworkVpDays(
    val today: NetworkVpDay,
    val tomorrow: NetworkVpDay
)
data class NetworkVpSubstitution(
    val id: Int = 0,
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String,
    val isDeleted: Boolean,
    @param:Json(name = "VpType") val vpType: String,
    val courseName: String,

    val targetDate: String,
    val createdAt: String,
    val updatedAt: String
)

data class NetworkVpInfoNeu(
    val id: Int,
    val text: String,
    val targetDate: String
)

data class NetworkVpDay(
    val substitutions: List<NetworkVpSubstitution> = emptyList(),
    val targetDate: String, // TODO: not a string
    val dayString: String,
    val info: List<NetworkVpInfoNeu>?
)

data class NetworkFeatureFlag(
    val featureFlags: Map<String, Boolean>
)