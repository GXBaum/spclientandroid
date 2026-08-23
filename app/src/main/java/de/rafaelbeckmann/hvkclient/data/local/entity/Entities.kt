package de.rafaelbeckmann.hvkclient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_flag")
data class FeatureFlagEntity(
    @PrimaryKey val key: String,
    val value: Boolean
)

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
