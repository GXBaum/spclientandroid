package de.rafaelbeckmann.hvkclient.features.courses.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserCourseEntity(
    @PrimaryKey val courseId: Int,
    val name: String,
)

@Entity
data class UserMarkEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val date: String,
    val grade: String,
    val course_id: Int,
    val half_year: Int,
    val isDeleted: Boolean
)
