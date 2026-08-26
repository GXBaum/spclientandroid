package de.rafaelbeckmann.hvkclient.features.vp.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import androidx.room.Relation
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpType
import kotlin.time.Instant


@Entity
data class VpSelectedCourseEntity(
    @PrimaryKey val id: String,
    val courseName: String,
    val verified: Boolean
)

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

@Entity
data class VpDayEntity(
    @PrimaryKey val targetDate: Instant,
    val dayString: String
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

data class VpDayWithInfo(
    @Embedded val day: VpDayEntity,
    @Relation(
        parentColumn = "targetDate",
        entityColumn = "targetDate"
    )
    val info: List<VpDayInfoItem>
)