package de.rafaelbeckmann.hvkclient.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import de.rafaelbeckmann.hvkclient.UserPreferences
import de.rafaelbeckmann.hvkclient.UserPreferencesSerializer
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.features.courses.data.CourseDao
import de.rafaelbeckmann.hvkclient.features.other.data.OtherDao
import de.rafaelbeckmann.hvkclient.features.vp.data.VpDao
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.net.CookieManager
import java.net.CookiePolicy

@Module
actual class PlatformModule {
    @Single
    fun provideHttpClientEngine(): HttpClientEngine = OkHttp.create()

    @Single
    @Named("spAuthTest")
    fun provideSpAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val cookieManager = CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }

        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor(loggingInterceptor)
            .build()
    }




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





    private val DATASTORE_NAME = "local"

    @Single
    @Named("preferences")
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO),
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }


    private val ENCRYPTED_DATASTORE_NAME = "encrypted"

    @Single
    @Named("encrypted")
    fun provideUserPreferencesDataStore(context: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            produceFile = { context.dataStoreFile(ENCRYPTED_DATASTORE_NAME) },
            serializer = UserPreferencesSerializer
        )
    }
}