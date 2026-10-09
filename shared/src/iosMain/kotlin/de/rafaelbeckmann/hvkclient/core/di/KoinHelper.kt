package de.rafaelbeckmann.hvkclient.core.di

import de.rafaelbeckmann.hvkclient.ConnectivityViewModel
import de.rafaelbeckmann.hvkclient.MainViewModel
import de.rafaelbeckmann.hvkclient.OnboardingViewModel
import de.rafaelbeckmann.hvkclient.SettingsViewModel
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.features.courses.presentation.CourseDetailViewModel
import de.rafaelbeckmann.hvkclient.features.courses.presentation.CoursesViewModel
import de.rafaelbeckmann.hvkclient.features.vp.presentation.VpViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object KoinHelper : KoinComponent {
    val vpViewModel: VpViewModel get() = get()
    val settingsViewModel: SettingsViewModel get() = get()
    val onboardingViewModel: OnboardingViewModel get() = get()
    val mainViewModel: MainViewModel get() = get()
    val coursesViewModel: CoursesViewModel get() = get()
    val courseDetailViewModel: CourseDetailViewModel get() = get()
    val connectivityViewModel: ConnectivityViewModel get() = get()
    val settingsRepository: SettingsRepository get() = get()
}
