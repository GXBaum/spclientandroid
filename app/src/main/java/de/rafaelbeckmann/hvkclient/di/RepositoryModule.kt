package de.rafaelbeckmann.hvkclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rafaelbeckmann.hvkclient.data.repository.EncryptedUserPreferencesRepositoryImpl
import de.rafaelbeckmann.hvkclient.data.repository.SettingsRepositoryImpl
import de.rafaelbeckmann.hvkclient.data.repository.SpRepositoryTestImpl
import de.rafaelbeckmann.hvkclient.domain.repository.EncryptedUserPreferencesRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SpRepositoryTest
import de.rafaelbeckmann.hvkclient.features.auth.data.AuthRepositoryImpl
import de.rafaelbeckmann.hvkclient.features.auth.domain.AuthRepository
import de.rafaelbeckmann.hvkclient.features.courses.data.CoursesRepositoryImpl
import de.rafaelbeckmann.hvkclient.features.courses.domain.CoursesRepository
import de.rafaelbeckmann.hvkclient.features.other.data.OtherStuffRepositoryImpl
import de.rafaelbeckmann.hvkclient.features.other.domain.OtherStuffRepository
import de.rafaelbeckmann.hvkclient.features.vp.data.VpRepositoryImpl
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCoursesRepository(
        impl: CoursesRepositoryImpl
    ): CoursesRepository

    @Binds
    @Singleton
    abstract fun bindVpRepository(
        impl: VpRepositoryImpl
    ): VpRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOtherStuffRepository(
        impl: OtherStuffRepositoryImpl
    ): OtherStuffRepository

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