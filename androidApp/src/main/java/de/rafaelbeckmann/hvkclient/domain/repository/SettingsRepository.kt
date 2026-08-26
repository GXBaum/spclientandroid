package de.rafaelbeckmann.hvkclient.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun setUserId(userId: String)
    suspend fun getUserId(): String?

    suspend fun setIsDeveloper(isDeveloper: Boolean)
    suspend fun isDeveloper(): Boolean

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun isOnboardingCompleted(): Boolean

    /*
    suspend fun setVpSelectedCourseName(courseName: String)
    suspend fun getVpSelectedCourseName(): String?

     */

    // TODO: Store Token in Encrypted DataStore
    suspend fun setAccessToken(token: String)
    suspend fun getAccessToken(): String?

    suspend fun setRefreshToken(token: String)
    suspend fun getRefreshToken(): String?


    fun useDynamicColorFlow(): Flow<Boolean>
    suspend fun setUseDynamicColor(enabled: Boolean)
}