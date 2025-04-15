package com.example.hvkclientmitbenachrichtigungen.data.model

import kotlinx.serialization.Serializable

data class TokenUpdateRequest(
    val token: String,
    val spUsername: String
)

data class UserCourses(
    val courses: List<UserCourse>
)
data class UserCourse(
    val course_id: Int,
    val name: String,
)