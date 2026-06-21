package de.rafaelbeckmann.hvkclient.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable


// TODO RENAME ALL SINGLE SCREENS TO ROUTE OR SOMETHING ELSE TO FIX AMBIGUITY BETWEEN THE COMPOSABLES
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
object TestGraph : NavGraphSpec {
    override fun startDestination() = ChatScreen
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
data class VpWebViewRoute(
    val course: String? = null
)

@Serializable
object LibrariesScreen

@Serializable
object ChatScreen

@Serializable
data class ChatDetailScreen(
    val chatId: String
)


data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int,
    val screenObject: Any
)