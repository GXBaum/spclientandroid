package de.rafaelbeckmann.hvkclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.model.UserCourseEntity
import de.rafaelbeckmann.hvkclient.data.model.UserMarkEntity
import de.rafaelbeckmann.hvkclient.data.model.VpDayEntity
import de.rafaelbeckmann.hvkclient.data.model.VpDayInfoItem
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourseEntity
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionEntity

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
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}