package de.rafaelbeckmann.hvkclient.di

import android.content.Context
import androidx.room.Room
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.features.courses.data.CourseDao
import de.rafaelbeckmann.hvkclient.features.other.data.OtherDao
import de.rafaelbeckmann.hvkclient.features.vp.data.VpDao
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
object DatabaseModule {

    @Single
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hvk-client-database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Single
    fun provideVpDao(appDatabase: AppDatabase): VpDao {
        return appDatabase.vpDao()
    }

    @Single
    fun provideCourseDao(appDatabase: AppDatabase): CourseDao {
        return appDatabase.courseDao()
    }

    @Single
    fun provideOtherDao(appDatabase: AppDatabase): OtherDao {
        return appDatabase.otherDao()
    }
}