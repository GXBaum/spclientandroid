package de.rafaelbeckmann.hvkclient.data.repository

import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val prefUtils: PrefUtils
) : SettingsRepository {

    override suspend fun setUsername(username: String) {
        prefUtils.saveString("username", username)
    }

    override suspend fun setIsDeveloper(isDeveloper: Boolean) {
        prefUtils.saveString("isDeveloper", isDeveloper.toString())
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefUtils.saveString("onboardingCompleted", completed.toString())
    }

    override suspend fun setVpSelectedCourseName(courseName: String) {
        prefUtils.saveString("vpSelectedCourseName", courseName)
    }
}