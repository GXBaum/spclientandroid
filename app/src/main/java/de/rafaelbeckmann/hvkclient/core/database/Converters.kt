package de.rafaelbeckmann.hvkclient.core.database

import androidx.room.TypeConverter
import kotlin.time.Instant

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