package de.rafaelbeckmann.hvkclient.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rafaelbeckmann.hvkclient.data.model.Chat
import de.rafaelbeckmann.hvkclient.data.model.ChatMessage
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpClassConverter
import de.rafaelbeckmann.hvkclient.data.model.VpInfoItem
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsCache

@Database(
    entities = [
        UserCourse::class,
        UserMark::class,
        VpSelectedCourse::class,
        VpSubstitution::class,
        VpSubstitutionsCache::class,
        FeatureFlagEntity::class,
        VpInfoItem::class,
        Chat::class,
        ChatMessage::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(VpClassConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}