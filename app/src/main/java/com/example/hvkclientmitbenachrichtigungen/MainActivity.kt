package com.example.hvkclientmitbenachrichtigungen

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.Manifest
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.hvkclientmitbenachrichtigungen.data.model.UserCourse
import com.example.hvkclientmitbenachrichtigungen.ui.theme.HvKClientMitBenachrichtigungenTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.div

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

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
                            Text("Screen B")
                            val args = it.toRoute<CourseDetailsScreen>()

                            CourseDetailScreen(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                courseId = args.courseId,
                            )

                            /*Button(
                                onClick = {
                                    navController.navigate(CoursesScreen)
                                },
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text("Go to Screen A with mark: ${args.name}")
                            }*/
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



