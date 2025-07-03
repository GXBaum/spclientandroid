package de.rafaelbeckmann.hvkclient.data.repository

import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import javax.inject.Inject

object PreferenceKeys {
    const val USERNAME = "username"
    const val IS_DEVELOPER = "is_developer"
    const val IS_ONBOARDING_COMPLETED = "is_onboarding_completed"
    const val VP_SELECTED_COURSE_NAME = "vp_selected_course_name"
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

    override suspend fun setVpSelectedCourseName(courseName: String) {
        prefUtils.saveString(PreferenceKeys.VP_SELECTED_COURSE_NAME, courseName)
    }

    override suspend fun getVpSelectedCourseName(): String? {
        return prefUtils.getString(PreferenceKeys.VP_SELECTED_COURSE_NAME)
    }
}