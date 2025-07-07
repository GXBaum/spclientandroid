package de.rafaelbeckmann.hvkclient.ui.main

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
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
import de.rafaelbeckmann.hvkclient.PrefUtils
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.ui.navigation.AppNavHost
import de.rafaelbeckmann.hvkclient.ui.navigation.OnboardingGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.navigation.VpScreen

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun MainScreen(
    settingsRepository: SettingsRepository,
    prefUtils: PrefUtils,
    intent: Intent
) {
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()

    // Handle navigation based on intent extras (Deep Links)
    LaunchedEffect(Unit) {

        //TODO: change to real deep link handling (https://medium.com/androiddevelopers/type-safe-navigation-for-compose-105325a97657, https://developer.android.com/guide/navigation/design/deep-link)
        val navigateToRevealMark = intent.getBooleanExtra("navigate_to_reveal_mark", false)
        val grade = intent.getStringExtra("grade")
        val navigateToVp = intent.getBooleanExtra("navigate_to_vp", false)

        when {
            navigateToRevealMark && grade != null -> {
                Log.d("DeepLink", "Navigating to grade reveal screen with grade: $grade")
                intent.removeExtra("navigate_to_reveal_mark")
                navController.navigate(RevealMarkScreen(grade))
            }
            navigateToVp -> {
                Log.d("DeepLink", "Navigating to vp screen")
                intent.removeExtra("navigate_to_vp")
                navController.navigate(VpScreen)
            }
        }
    }

    var isOnboardingCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // TODO: keine ahnung ob das gut ist
        isOnboardingCompleted = settingsRepository.isOnboardingCompleted()

        if (!isOnboardingCompleted) {
            navController.navigate(OnboardingGraph)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isRevealMarkScreen = currentDestination?.hasRoute(RevealMarkScreen::class) == true
    val isOnboarding = currentDestination?.hierarchy?.any { it.hasRoute(OnboardingGraph::class) } == true

    val showStatusBar = !isRevealMarkScreen

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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            scope = scope,
            prefUtils = prefUtils
        )
    }
}