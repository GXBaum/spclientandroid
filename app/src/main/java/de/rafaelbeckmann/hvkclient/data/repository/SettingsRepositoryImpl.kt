package de.rafaelbeckmann.hvkclient.data.repository

import android.util.Log
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

object PreferenceKeys {
    const val USERNAME = "username"
    const val IS_DEVELOPER = "is_developer"
    const val IS_ONBOARDING_COMPLETED = "is_onboarding_completed"
    const val ACCESS_TOKEN = "access_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val USE_DYNAMIC_COLOR = "use_dynamic_color"
}

@Single(binds = [SettingsRepository::class])
class SettingsRepositoryImpl(
    private val prefUtils: PrefUtils
) : SettingsRepository {

    override suspend fun setUserId(userId: String) {
        prefUtils.saveString(PreferenceKeys.USERNAME, userId)
    }

    override suspend fun getUserId(): String? {
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

    override fun useDynamicColorFlow(): Flow<Boolean> {
        return prefUtils.booleanFlow(PreferenceKeys.USE_DYNAMIC_COLOR, default = true)
    }

    override suspend fun setUseDynamicColor(enabled: Boolean) {
        prefUtils.saveString(PreferenceKeys.USE_DYNAMIC_COLOR, enabled.toString())
    }
}