package de.rafaelbeckmann.hvkclient.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
object VpGraph

@Serializable
object CoursesGraph

@Serializable
object SettingsGraph

@Serializable
object OnboardingGraph

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
object VpScreen

@Serializable
object OnboardingScreen

@Serializable
object OnboardingScreenPage2

@Serializable
object OnboardingScreenPage3


data class navItem(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int,
    val screenObject: Any
)