package de.rafaelbeckmann.hvkclient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feature_flag")
data class FeatureFlagEntity(
    @PrimaryKey val key: String,
    val value: Boolean
)