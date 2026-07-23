package de.rafaelbeckmann.hvkclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.repository.EncryptedUserPreferencesRepositoryImpl
import de.rafaelbeckmann.hvkclient.data.repository.HvkRepositoryImpl
import de.rafaelbeckmann.hvkclient.data.repository.SettingsRepositoryImpl
import de.rafaelbeckmann.hvkclient.data.repository.SpRepositoryTestImpl
import de.rafaelbeckmann.hvkclient.domain.repository.EncryptedUserPreferencesRepository
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SpRepositoryTest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHvkRepository(
        impl: HvkRepositoryImpl
    ): HvkRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindSpTestRepository(
        impl: SpRepositoryTestImpl
    ): SpRepositoryTest

    @Binds
    @Singleton
    abstract fun bindEncryptedUserPreferencesRepository(
        impl: EncryptedUserPreferencesRepositoryImpl
    ): EncryptedUserPreferencesRepository
}