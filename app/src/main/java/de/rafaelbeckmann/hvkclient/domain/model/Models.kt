package de.rafaelbeckmann.hvkclient.domain.model

data class FeatureFlag(
    val featureFlags: Map<String, Boolean>
)

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
