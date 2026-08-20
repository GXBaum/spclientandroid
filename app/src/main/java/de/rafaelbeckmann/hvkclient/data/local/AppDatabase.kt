package de.rafaelbeckmann.hvkclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rafaelbeckmann.hvkclient.data.local.entity.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.UserCourseEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.UserMarkEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.VpDayEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.VpDayInfoItem
import de.rafaelbeckmann.hvkclient.data.local.entity.VpSelectedCourseEntity
import de.rafaelbeckmann.hvkclient.data.local.entity.VpSubstitutionEntity
import de.rafaelbeckmann.hvkclient.data.model.Converters

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