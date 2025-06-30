package de.rafaelbeckmann.hvkclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsAllCache
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsAllConverter

@Database(
    entities = [UserCourse::class, UserMark::class, VpSelectedCourse::class, VpSubstitution::class, VpSubstitutionsAllCache::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(VpSubstitutionsAllConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

