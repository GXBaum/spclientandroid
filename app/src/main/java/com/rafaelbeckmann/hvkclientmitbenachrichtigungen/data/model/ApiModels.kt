package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model

data class TokenUpdateRequest(
    val token: String,
    val spUsername: String
)

data class UserCourses(
    val courses: List<UserCourse>
)
data class UserCourse(
    val courseId: Int,
    val name: String,
)

data class UserMarks(
    val marks: List<UserMark>
)
data class UserMark(
    val mark_id: Int,
    val name: String,
    val date: String,
    val grade: String,
    val course_id: Int,
    val sp_username: String,
    val half_year: Int
)



data class VpSelectedCourse(
    val courseName: String
)

data class VpSubstitutions(
    val substitutions: List<VpSubstitution>
)

data class VpSubstitution(
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String
)


