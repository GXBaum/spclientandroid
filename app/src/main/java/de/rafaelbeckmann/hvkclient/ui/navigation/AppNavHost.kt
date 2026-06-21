package de.rafaelbeckmann.hvkclient.ui.navigation

import android.os.Build
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import de.rafaelbeckmann.hvkclient.ui.chat.ChatDetailScreen
import de.rafaelbeckmann.hvkclient.ui.chat.ChatScreen
import de.rafaelbeckmann.hvkclient.ui.common.AddCourseScreen
import de.rafaelbeckmann.hvkclient.ui.coursedetail.CourseDetailScreen
import de.rafaelbeckmann.hvkclient.ui.courses.CoursesScreen
import de.rafaelbeckmann.hvkclient.ui.onboarding.NotificationPermissionPromptScreen
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreen
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreenLogin
import de.rafaelbeckmann.hvkclient.ui.onboarding.OnboardingScreenSetup
import de.rafaelbeckmann.hvkclient.ui.revealmark.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.settings.LibrariesScreen
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsScreen
import de.rafaelbeckmann.hvkclient.ui.vp.VpScreen
import de.rafaelbeckmann.hvkclient.ui.vp.VpWebViewScreen


const val VP_FAB_EXPLODE_BOUND = "VP_FAB_EXPLODE_BOUND"

// TODO: inject via Hilt
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Any, // TODO: change type
    modifier: Modifier = Modifier,
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = startDestination,
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
            // TODO: vlt müssen die deepLinks sinnvoller benannt werden
            navigation<VpGraph>(
                startDestination = VpGraph.startDestination(),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "hvkclient://vp" },
                    //navDeepLink { uriPattern = "https://rafaelbeckmann.de/hvkclient/vp" }
                )
            ) {
                composable<VpScreen> (
                    deepLinks = listOf(
                        navDeepLink<VpScreen>(
                            basePath = "hvkclient://vpScreen"
                        )
                    )
                ) {
                    val args = it.toRoute<VpScreen>()

                    VpScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        course = args.course,
                        onVpOpenClick = { course ->
                            navController.navigate(
                                VpWebViewRoute(
                                    course = course
                                )
                            )
                        },
                        animatedVisibilityScope = this
                    )
                }

                composable<VpWebViewRoute> {
                    val args = it.toRoute<VpWebViewRoute>()

                    VpWebViewScreen(
                        course = args.course,
                        modifier = Modifier
                            .fillMaxSize()
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(
                                    key = VP_FAB_EXPLODE_BOUND
                                ),
                                animatedVisibilityScope = this
                            )
                    )
                }
            }

            navigation<CoursesGraph>(
                startDestination = CoursesGraph.startDestination()
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
                ) {
                    val args = it.toRoute<RevealMarkScreen>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        RevealMarkScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            grade = args.grade
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Dieses Feature benötigt Android 14 oder höher.")
                        }
                    }
                }
            }

            navigation<SettingsGraph>(
                startDestination = SettingsGraph.startDestination(),
                deepLinks = listOf(
                    navDeepLink<SettingsGraph>(
                        basePath = "hvkclient://settings"
                    )
                )
            ) {
                composable<SettingsScreen> {
                    SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        onAddCourseClick = { navController.navigate(AddCourseScreen) },
                        onLibrariesClick = { navController.navigate(LibrariesScreen) }
                    )
                }
                composable<AddCourseScreen> {
                    AddCourseScreen(
                        onBack = { navController.popBackStack() },
                        onContinue = { navController.popBackStack() }
                    )
                }
                composable<LibrariesScreen> {
                    LibrariesScreen()
                }
            }

            navigation<TestGraph>(
                startDestination = TestGraph.startDestination()
            ) {
                composable<ChatScreen>{
                    ChatScreen(
                        onChatClick = { chatId ->
                            navController.navigate(
                                ChatDetailScreen(chatId)
                            )
                        }
                    )
                }
                composable<ChatDetailScreen>{
                    val args = it.toRoute<ChatDetailScreen>()

                    ChatDetailScreen(
                        chatId = args.chatId
                    )
                }
            }

            navigation<OnboardingGraph>(
                // TODO: das ist nur provisorisch
                //startDestination = OnboardingScreen
                startDestination = OnboardingGraph.startDestination()
            ) {
                composable<OnboardingScreen> {
                    OnboardingScreen(
                        onContinueClicked = {
                            navController.navigate(OnboardingScreenSetup)
                        },
                        onLoginClicked = {
                            navController.navigate(OnboardingScreenLogin)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
                composable<OnboardingScreenSetup> {
                    OnboardingScreenSetup(
                        modifier = Modifier.fillMaxSize(),
                        onContinueClicked = {
                            navController.navigate(VpGraph) {
                                popUpTo(OnboardingGraph) {
                                    inclusive = true
                                }
                            }
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }
                composable<OnboardingScreenLogin> {
                    OnboardingScreenLogin(
                        modifier = Modifier.fillMaxSize(),
                        onContinueClicked = {
                            navController.navigate(VpGraph) {
                                popUpTo(OnboardingGraph) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
                composable<NotificationPermissionPromptScreen> {
                    NotificationPermissionPromptScreen(
                        onProceed = {
                            navController.navigate(OnboardingScreen)
                        }
                    )
                }
            }
        }
    }
}