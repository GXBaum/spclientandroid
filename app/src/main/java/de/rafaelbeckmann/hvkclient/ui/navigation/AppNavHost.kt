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
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.ui.coursedetail.CourseDetailScreen
import de.rafaelbeckmann.hvkclient.ui.courses.CoursesScreen
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreen
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreenPage2
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreenPage3
import de.rafaelbeckmann.hvkclient.ui.revealmark.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsScreen
import de.rafaelbeckmann.hvkclient.ui.vp.VpScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// TODO: inject via Hilt
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
            fadeIn(animationSpec = tween(durationMillis = 100))

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
            fadeOut(animationSpec = tween(durationMillis = 100))


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
        // TODO: vlt müssen die deepLinks sinnvollerf benannt werden
        navigation<VpGraph>(
            startDestination = VpScreen(""),
            deepLinks = listOf(
                navDeepLink<VpGraph>(
                    basePath = "hvkclient://vp"
                )
            )
        ) {
            composable<VpScreen> (
                deepLinks = listOf(
                    navDeepLink<VpScreen>(
                        basePath = "hvkclient://vpScreen"
                    )
                )
            ){
                val args = it.toRoute<VpScreen>()

                VpScreen(
                    modifier = Modifier
                        .fillMaxSize(),
                    course = args.course
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
                    modifier = Modifier.fillMaxSize(),
                    courseId = args.courseId,
                    onNavigateToRevealMark = { grade ->
                        navController.navigate(
                            RevealMarkScreen(grade)
                        )
                    }
                )
            }

            // TODO: soll die Note ein query argument sein?
            composable<RevealMarkScreen> (
                deepLinks = listOf(
                    navDeepLink<RevealMarkScreen>(
                        basePath = "hvkclient://revealmark"
                    )
                )
            ){
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
            startDestination = SettingsScreen,
            deepLinks = listOf(
                navDeepLink<SettingsGraph>(
                    basePath = "hvkclient://settings"
                )
            )
        ) {
            composable<SettingsScreen> {
                SettingsScreen(
                    modifier = Modifier.fillMaxSize()
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
                    onLoginClicked = {
                        navController.navigate(OnboardingScreenPage3)
                    },
                    onCreateAccountClicked = {
                        TODO("Navigate to create account screen")
                    }

                )
            }
            composable<OnboardingScreenPage3> {
                OnboardingScreenPage3(
                    modifier = Modifier.fillMaxSize(),
                    onContinueClicked = {
                        scope.launch {
                            // TODO: use repository instead
                            prefUtils.saveString("is_onboarding_completed", "true")
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