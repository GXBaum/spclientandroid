package com.rafaelbeckmann.hvkclientmitbenachrichtigungen

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import androidx.annotation.RequiresApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.coursedetail.CourseDetailScreen
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.courses.CoursesScreen
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.revealmark.RevealMarkScreen
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.theme.HvKClientMitBenachrichtigungenTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        enableEdgeToEdge()

        setContent {
            HvKClientMitBenachrichtigungenTheme {

                val navController = rememberNavController()
                Scaffold(
                    contentWindowInsets = WindowInsets(0.dp),
                    modifier = Modifier.fillMaxSize(),
                    topBar = { },
                    bottomBar = { }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = CoursesScreen,
                        modifier = Modifier.fillMaxSize()
                            .padding(innerPadding),

                        enterTransition = {
                            fadeIn() + slideInHorizontally(initialOffsetX = { it * 1/3 })
                        },
                        exitTransition = {
                            fadeOut() + slideOutHorizontally(targetOffsetX = { -it * 1/3 })
                        },
                        popEnterTransition = {
                            fadeIn() + slideInHorizontally(initialOffsetX = { -it * 1/3 })
                        },
                        popExitTransition = {
                           fadeOut() + slideOutHorizontally(targetOffsetX = { it * 1/3 })

                            /*scaleOut(
                                targetScale = 0.9f,
                                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
                            )*/
                        }
                    ) {

                        composable<CoursesScreen> {
                            CoursesScreen(
                                onCourseClick = { course ->
                                    navController.navigate(
                                        CourseDetailsScreen(
                                            name = course.name,
                                            courseId = course.courseId
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

