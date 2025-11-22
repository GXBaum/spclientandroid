package de.rafaelbeckmann.hvkclient.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable


interface NavGraphSpec {
    fun startDestination() : Any
}

@Serializable
object VpGraph : NavGraphSpec {
    override fun startDestination() = VpScreen()
}

@Serializable
object CoursesGraph : NavGraphSpec {
    override fun startDestination() = CoursesScreen
}

@Serializable
object SettingsGraph : NavGraphSpec {
    override fun startDestination() = SettingsScreen
}

@Serializable
object OnboardingGraph : NavGraphSpec {
    override fun startDestination() = NotificationPermissionPromptScreen // TODO: das ist nur provisorisch
}

@Serializable
object CoursesScreen

@Serializable
data class CourseDetailsScreen(
    val name: String,
    val courseId: Int
)

@Serializable
data class RevealMarkScreen(
    val grade: String
)

@Serializable
object SettingsScreen

@Serializable
data class VpScreen(
    val course: String? = null
)

@Serializable
object OnboardingScreen

@Serializable
object OnboardingScreenSetup

@Serializable
object OnboardingScreenLogin

@Serializable
object NotificationPermissionPromptScreen

@Serializable
object AddCourseScreen

@Serializable
object VpWebView

@Serializable
object LibrariesScreen


data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int,
    val screenObject: Any
)