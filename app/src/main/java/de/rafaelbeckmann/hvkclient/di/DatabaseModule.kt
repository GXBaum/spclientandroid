package de.rafaelbeckmann.hvkclient.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.features.courses.data.CourseDao
import de.rafaelbeckmann.hvkclient.features.other.data.OtherDao
import de.rafaelbeckmann.hvkclient.features.vp.data.VpDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hvk-client-database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideVpDao(appDatabase: AppDatabase): VpDao {
        return appDatabase.vpDao()
    }

    @Provides
    @Singleton
    fun provideCourseDao(appDatabase: AppDatabase): CourseDao {
        return appDatabase.courseDao()
    }

    @Provides
    @Singleton
    fun provideOtherDao(appDatabase: AppDatabase): OtherDao {
        return appDatabase.otherDao()
    }
}