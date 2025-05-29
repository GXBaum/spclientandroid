package de.rafaelbeckmann.hvkclient

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.ui.coursedetail.CourseDetailScreen
import de.rafaelbeckmann.hvkclient.ui.courses.CoursesScreen
import de.rafaelbeckmann.hvkclient.ui.revealmark.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsScreen
import de.rafaelbeckmann.hvkclient.ui.theme.HvKClientTheme
import de.rafaelbeckmann.hvkclient.ui.vp.VpScreen
import kotlinx.coroutines.launch
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


                var actionBarTitle by rememberSaveable { mutableStateOf("HvK Client") }

                /*LaunchedEffect(navController) {
                    navController.currentBackStackEntryFlow.collect { backStackEntry ->
                        // You can map the title based on the route using:
                        actionBarTitle = getTitleByRoute(context, backStackEntry.destination.route)
                    }
                }*/

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
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = actionBarTitle,
                                    Modifier.clickable(
                                        onClick = {
                                            scope.launch {
                                                val saved = prefUtils.getString("isDeveloper")
                                                 if (saved == "true") {
                                                    prefUtils.saveString("isDeveloper", "false")
                                                    Toast.makeText(
                                                        context,
                                                        "Du bist jetzt wieder im normalen Modus",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                     prefUtils.saveString("isDeveloper", "true")
                                                     Toast.makeText(
                                                         context,
                                                         "Du bist jetzt im Debug Modus (No Diddy)",
                                                         Toast.LENGTH_LONG
                                                     ).show()
                                                 }
                                            }
                                        }
                                    )
                                )

                                Button(
                                    onClick = {
                                        navController.navigate(
                                            VpScreen
                                        )
                                    }
                                ) {
                                    Text("Vertretungsplan")
                                }
                            },
                            actions = {
                                Text(
                                    text = "Einstellungen",
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .clickable {
                                            navController.navigate(SettingsScreen)
                                        }
                                )
                            },
                        )
                    },
                    bottomBar = { }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = CoursesScreen,
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

                        composable<SettingsScreen> {
                            SettingsScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }

                        composable<VpScreen> {
                            VpScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                            )
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