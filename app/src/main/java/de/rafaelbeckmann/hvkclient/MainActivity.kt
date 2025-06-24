package de.rafaelbeckmann.hvkclient

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.ui.coursedetail.CourseDetailScreen
import de.rafaelbeckmann.hvkclient.ui.courses.CoursesScreen
import de.rafaelbeckmann.hvkclient.ui.revealmark.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsScreen
import de.rafaelbeckmann.hvkclient.ui.theme.HvKClientTheme
import de.rafaelbeckmann.hvkclient.ui.vp.VpScreen
import kotlinx.serialization.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefUtils: PrefUtils

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        enableEdgeToEdge()

        val navigateToRevealMark = intent.getBooleanExtra("navigate_to_reveal_mark", false)
        val grade = intent.getStringExtra("grade")

        val navigateToVp = intent.getBooleanExtra("navigate_to_vp", false)


        setContent {
            HvKClientTheme {
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                val navController = rememberNavController()

                // Handle deep link from notification
                LaunchedEffect(Unit) {
                    Log.d("test123", "Received navigateToRevealMark: $navigateToRevealMark, grade: $grade")

                    if (navigateToRevealMark && grade != null) {
                        Log.d("test123", "Navigating to grade reveal screen with grade: $grade")

                        // Reset the flags to prevent re-navigation on config changes
                        intent.removeExtra("navigate_to_reveal_mark")

                        // Navigate to RevealMarkScreen with the grade
                        navController.navigate(
                            RevealMarkScreen(grade)
                        )
                    }

                    if (navigateToVp) {
                        intent.removeExtra("navigate_to_vp")
                        navController.navigate(VpScreen)
                    }
                }

                Scaffold(
                    //contentWindowInsets = WindowInsets(0.dp),
                    //contentWindowInsets = WindowInsets.safeDrawing,
                    contentWindowInsets = WindowInsets.statusBars,

                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        val navItemList = listOf(
                            navItem("Vertretungsplan", Icons.Default.Home, 69, VpGraph),
                            navItem("SP Noten", Icons.Default.Grade, 0, CoursesGraph),
                            navItem("Einstellungen", Icons.Default.Settings, 0, SettingsGraph)
                        )

                        NavigationBar {
                            navItemList.forEach { navItem ->
                                val isSelected = currentDestination?.hierarchy?.any {
                                    it.route == navItem.screenObject::class.qualifiedName
                                } == true

                                NavigationBarItem(
                                    icon = {
                                        BadgedBox(badge = {
                                            if (navItem.badgeCount > 0) {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(text = navItem.badgeCount.toString())
                                                }
                                            }
                                        }) {
                                            Icon(navItem.icon, contentDescription = navItem.label)
                                        }
                                    },
                                    label = { Text(navItem.label) },
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(navItem.screenObject) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = VpGraph,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),

                        /*
                        enterTransition = {
                            fadeIn() + slideInHorizontally(initialOffsetX = { it * 1 / 3 })
                        },
                        exitTransition = {
                            fadeOut() + slideOutHorizontally(targetOffsetX = { -it * 1 / 3 })
                        },
                        popEnterTransition = {
                            fadeIn() + slideInHorizontally(initialOffsetX = { -it * 1 / 3 })
                        },
                        popExitTransition = {
                           fadeOut() + slideOutHorizontally(targetOffsetX = { it * 1 / 3 })

                            /*scaleOut(
                                targetScale = 0.9f,
                                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
                            )*/
                        }
                        */

                        enterTransition = {
                            fadeIn(animationSpec = tween(durationMillis = 200)) +
                                    slideInHorizontally(
                                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                        initialOffsetX = { fullWidth -> fullWidth / 3 }
                                    ) +
                                    scaleIn(animationSpec = tween(durationMillis = 200), initialScale = 0.95f)
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(durationMillis = 200)) +
                                    slideOutHorizontally(
                                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                        targetOffsetX = { fullWidth -> -fullWidth / 3 }
                                    ) +
                                    scaleOut(animationSpec = tween(durationMillis = 200), targetScale = 0.95f)
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(durationMillis = 200)) +
                                    slideInHorizontally(
                                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                        initialOffsetX = { fullWidth -> -fullWidth / 3 }
                                    ) +
                                    scaleIn(animationSpec = tween(durationMillis = 200), initialScale = 0.95f)
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(durationMillis = 200)) +
                                    slideOutHorizontally(
                                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                        targetOffsetX = { fullWidth -> fullWidth / 3 }
                                    ) +
                                    scaleOut(animationSpec = tween(durationMillis = 200), targetScale = 0.95f)
                        }
                    ) {
                        navigation<VpGraph>(
                            startDestination = VpScreen
                        ) {
                            composable<VpScreen> {
                                VpScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }
                        }

                        navigation<CoursesGraph>(
                            startDestination = CoursesScreen
                        ) {
                            composable<CoursesScreen> {
                                CoursesScreen(
                                    onCourseClick = { course ->
                                        navController.navigate(
                                            CourseDetailsScreen(
                                                name = course.name,
                                                courseId = course.courseId,
                                            )
                                        )
                                    }
                                )
                            }

                            composable<CourseDetailsScreen> {
                                val args = it.toRoute<CourseDetailsScreen>()

                                CourseDetailScreen(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    courseId = args.courseId,
                                    onNavigateToRevealMark = { grade ->
                                        navController.navigate(
                                            RevealMarkScreen(grade)
                                        )
                                    }
                                )
                            }

                            composable<RevealMarkScreen> {
                                val args = it.toRoute<RevealMarkScreen>()
                                RevealMarkScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White),
                                    grade = args.grade
                                )
                            }
                        }

                        navigation<SettingsGraph>(
                            startDestination = SettingsScreen
                        ) {
                            composable<SettingsScreen> {
                                SettingsScreen(
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }
}


@Serializable
object VpGraph

@Serializable
object CoursesGraph

@Serializable
object SettingsGraph

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


data class navItem(
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int,
    val screenObject: Any
)