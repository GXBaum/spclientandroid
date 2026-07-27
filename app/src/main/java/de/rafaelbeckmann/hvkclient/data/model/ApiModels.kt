package de.rafaelbeckmann.hvkclient.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Instant

// TODO: split into multiple files


data class FeatureFlag(
    val featureFlags: Map<String, Boolean>
)

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

data class UserCourse(
    val id: Int,
    val name: String
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

data class UserMark(
    val id: Int,
    val name: String,
    val date: String,
    val grade: String,
    val courseId: Int,
    val halfYear: Int,
    val isDeleted: Boolean
)

@Entity
data class VpSelectedCourseEntity(
    @PrimaryKey val id: String,
    val courseName: String,
    val verified: Boolean
)

enum class VpType {
    substitution, differentRoom
}

@Entity
data class VpSubstitutionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String,
    val isDeleted: Boolean,
    val VpType: VpType,
    val courseName: String,

    val targetDate: Instant,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class VpSubstitution(
    val id: Int = 0,
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String,
    val isDeleted: Boolean,
    val VpType: VpType,
    val courseName: String,

    val targetDate: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class VpInfoNeu(
    val id: Int,
    val text: String,
    val targetDate: String
)

data class VpDay(
    val substitutions: List<VpSubstitution> = emptyList(),
    val targetDate: LocalDate,
    val dayString: String,
    val info: List<VpInfoNeu>?
)

@Entity
data class VpDayEntity(
    @PrimaryKey val targetDate: Instant,
    val dayString: String
)

data class VpDayWithInfo(
    @Embedded val day: VpDayEntity,
    @Relation(
        parentColumn = "targetDate",
        entityColumn = "targetDate"
    )
    val info: List<VpDayInfoItem>
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = VpDayEntity::class,
            parentColumns = ["targetDate"],
            childColumns = ["targetDate"],
            onDelete = CASCADE,
        )
    ]
)
data class VpDayInfoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetDate: Instant,
    val info: String
)

data class VpDays(
    val today: VpDay?,
    val tomorrow: VpDay?
)

// TODO: maybe move this
class Converters {
    @TypeConverter
    fun fromEpochMillis(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }
}