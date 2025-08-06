package de.rafaelbeckmann.hvkclient.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.AndroidAlarmScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    @Provides
    @Singleton
    fun provideAndroidAlarmScheduler(
        @ApplicationContext context: Context
    ): AndroidAlarmScheduler {
        return AndroidAlarmScheduler(context)
    }
}