package de.rafaelbeckmann.hvkclient.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.ui.coursedetail.CourseDetailScreen
import de.rafaelbeckmann.hvkclient.ui.courses.CoursesScreen
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreen
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreenPage2
import de.rafaelbeckmann.hvkclient.ui.revealmark.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsScreen
import de.rafaelbeckmann.hvkclient.ui.vp.VpScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    prefUtils: PrefUtils
) {
    NavHost(
        navController = navController,
        startDestination = VpGraph,
        modifier = modifier,

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
            fadeIn(animationSpec = tween(durationMillis = 200))

            /*
            fadeIn(animationSpec = tween(durationMillis = 200)) +
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        initialOffsetX = { fullWidth -> fullWidth / 3 }
                    ) +
                    scaleIn(animationSpec = tween(durationMillis = 200), initialScale = 0.95f)
             */
        },
        exitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 200))


            /*
            fadeOut(animationSpec = tween(durationMillis = 200)) +
                    slideOutHorizontally(
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> -fullWidth / 3 }
                    )// +
            //scaleOut(animationSpec = tween(durationMillis = 200), targetScale = 0.95f)
            */
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
                    )// +
            //scaleOut(animationSpec = tween(durationMillis = 200), targetScale = 0.95f)
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

        navigation<OnboardingGraph>(
            startDestination = OnboardingScreen
        ) {
            composable<OnboardingScreen> {
                OnboardingScreen(
                    onContinueClicked = {
                        navController.navigate(OnboardingScreenPage2)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            composable<OnboardingScreenPage2> {
                OnboardingScreenPage2(
                    modifier = Modifier.fillMaxSize(),
                    onContinueClicked = {
                        scope.launch {
                            prefUtils.saveString("onboarding_completed", "true")
                        }
                        navController.navigate(VpGraph) {
                            popUpTo(OnboardingGraph::class.qualifiedName!!) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}