package de.rafaelbeckmann.hvkclient.ui.main

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.rafaelbeckmann.hvkclient.ObserveAsEvents
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.SnackbarController
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.ui.navigation.AppNavHost
import de.rafaelbeckmann.hvkclient.ui.navigation.OnboardingGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.navigation.VpGraph
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun MainScreen(
    settingsRepository: SettingsRepository,
    prefUtils: PrefUtils,
    intent: Intent
) {
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()

    var isOnboardingCompleted by remember { mutableStateOf(false) }

    //TODO: change to real deep link handling (https://medium.com/androiddevelopers/type-safe-navigation-for-compose-105325a97657, https://developer.android.com/guide/navigation/design/deep-link)

    var startDestination by remember { mutableStateOf<Any>(VpGraph) }
    LaunchedEffect(Unit) {
        val completed = settingsRepository.isOnboardingCompleted()
        startDestination = if (completed) VpGraph else OnboardingGraph
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isRevealMarkScreen = currentDestination?.hasRoute(RevealMarkScreen::class) == true
    val isOnboarding = currentDestination?.hierarchy?.any { it.hasRoute(OnboardingGraph::class) } == true

    // TODO: regression in bottomPadding in VpWebView.kt (falsely adds navigation bar padding even though i have a NavBar Component in use, which consumes that space)
    //val isSettings = currentDestination?.hierarchy?.any { it.hasRoute(SettingsGraph::class) } == true
    //val isCourses = currentDestination?.hierarchy?.any { it.hasRoute(CoursesGraph::class) } == true

    //val showStatusBar = !isRevealMarkScreen
    val showStatusBar = false



    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    ObserveAsEvents(
        flow = SnackbarController.events,
        snackbarHostState
    ) { event ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()

            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.action?.name,
                duration = event.duration
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.action?.action?.invoke()
            }
        }
    }

    Scaffold(
        // TODO: WindowInsets(0.dp) für fullscreen
        //contentWindowInsets = WindowInsets(0.dp),
        //contentWindowInsets = WindowInsets.safeDrawing,
        //contentWindowInsets = WindowInsets.statusBars,

        contentWindowInsets = if (showStatusBar) {
            WindowInsets.statusBars
        } else {
            WindowInsets(0.dp)
        },

        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val showBottomBar = !isOnboarding && !isRevealMarkScreen

            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {

                // TODO: kann man das auch injecten?
                AppBottomNavigation(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { innerPadding ->

        // TODO: inject via Hilt
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            scope = scope,
            prefUtils = prefUtils
        )
    }
}
