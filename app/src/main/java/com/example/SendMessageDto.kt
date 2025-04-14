package com.example

data class SendMessageDto(
    val to: String?,
    val notification: NotificationBody
)
data class NotificationBody(
    val title: String,
    val body: String
)

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