package de.rafaelbeckmann.hvkclient.core.di

import de.rafaelbeckmann.hvkclient.NotificationTokenProviderImpl
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object KoinHelper : KoinComponent {
    val settingsRepository: SettingsRepository get() = get()
    val notificationTokenProvider: NotificationTokenProviderImpl get() = get()
}
