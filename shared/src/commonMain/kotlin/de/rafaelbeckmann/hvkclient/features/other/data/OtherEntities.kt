package de.rafaelbeckmann.hvkclient.features.other.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_flag")
data class FeatureFlagEntity(
    @PrimaryKey val key: String,
    val value: Boolean
)