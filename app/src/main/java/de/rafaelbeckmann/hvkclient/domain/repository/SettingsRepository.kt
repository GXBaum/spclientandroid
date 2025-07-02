package de.rafaelbeckmann.hvkclient.domain.repository

interface SettingsRepository {
    suspend fun setUsername(username: String)

    suspend fun setIsDeveloper(isDeveloper: Boolean)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setVpSelectedCourseName(courseName: String)
}