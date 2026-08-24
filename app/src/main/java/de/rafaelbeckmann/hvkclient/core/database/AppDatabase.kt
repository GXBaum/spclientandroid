package de.rafaelbeckmann.hvkclient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.features.courses.data.UserCourseEntity
import de.rafaelbeckmann.hvkclient.features.courses.data.UserMarkEntity
import de.rafaelbeckmann.hvkclient.features.other.data.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.features.vp.data.VpDayEntity
import de.rafaelbeckmann.hvkclient.features.vp.data.VpDayInfoItem
import de.rafaelbeckmann.hvkclient.features.vp.data.VpSelectedCourseEntity
import de.rafaelbeckmann.hvkclient.features.vp.data.VpSubstitutionEntity

@Database(
    entities = [
        UserCourseEntity::class,
        UserMarkEntity::class,
        VpSelectedCourseEntity::class,
        VpSubstitutionEntity::class,
        VpDayEntity::class,
        VpDayInfoItem::class,
        FeatureFlagEntity::class
    ],
    version = 5,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}