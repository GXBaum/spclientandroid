package de.rafaelbeckmann.hvkclient.features.courses.domain

data class UserCourse(
    val id: Int,
    val name: String
)

data class UserMark(
    val id: Int,
    val name: String,
    val date: String,
    val grade: String,
    val courseId: Int,
    val halfYear: Int,
    val isDeleted: Boolean
)