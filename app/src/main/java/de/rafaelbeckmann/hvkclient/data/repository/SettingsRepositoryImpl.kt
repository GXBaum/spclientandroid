package de.rafaelbeckmann.hvkclient.data.repository

import android.util.Log
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import javax.inject.Inject

object PreferenceKeys {
    const val USERNAME = "username"
    const val IS_DEVELOPER = "is_developer"
    const val IS_ONBOARDING_COMPLETED = "is_onboarding_completed"
    const val ACCESS_TOKEN = "access_token"
    const val REFRESH_TOKEN = "refresh_token"
}

class SettingsRepositoryImpl @Inject constructor(
    private val prefUtils: PrefUtils
) : SettingsRepository {

    override suspend fun setUsername(username: String) {
        prefUtils.saveString(PreferenceKeys.USERNAME, username)
    }

    override suspend fun getUsername(): String? {
        return prefUtils.getString(PreferenceKeys.USERNAME)
    }

    override suspend fun setIsDeveloper(isDeveloper: Boolean) {
        prefUtils.saveString(PreferenceKeys.IS_DEVELOPER, isDeveloper.toString())
    }

    override suspend fun isDeveloper(): Boolean {
        return prefUtils.getString(PreferenceKeys.IS_DEVELOPER)?.toBoolean() ?: false
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefUtils.saveString(PreferenceKeys.IS_ONBOARDING_COMPLETED, completed.toString())
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return prefUtils.getString(PreferenceKeys.IS_ONBOARDING_COMPLETED)?.toBoolean() ?: false
    }

    override suspend fun setAccessToken(token: String) {
        prefUtils.saveString(PreferenceKeys.ACCESS_TOKEN, token)
        Log.d("SettingsRepository", "Access token set: $token")
    }

    override suspend fun getAccessToken(): String? {
        Log.d("SettingsRepository", "Access token retrieved: ${prefUtils.getString(PreferenceKeys.ACCESS_TOKEN)}")
        return prefUtils.getString(PreferenceKeys.ACCESS_TOKEN)
    }

    override suspend fun setRefreshToken(token: String) {
        prefUtils.saveString(PreferenceKeys.REFRESH_TOKEN, token)
        Log.d("SettingsRepository", "Refresh token set: $token")
    }

    override suspend fun getRefreshToken(): String? {
        Log.d("SettingsRepository", "Refresh token retrieved: ${prefUtils.getString(PreferenceKeys.REFRESH_TOKEN)}")
        return prefUtils.getString(PreferenceKeys.REFRESH_TOKEN)
    }
}