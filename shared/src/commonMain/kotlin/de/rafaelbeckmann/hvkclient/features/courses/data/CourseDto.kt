package de.rafaelbeckmann.hvkclient.features.courses.data

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
