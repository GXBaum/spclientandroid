package de.rafaelbeckmann.hvkclient.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.rafaelbeckmann.hvkclient.ConnectivityViewModel
import de.rafaelbeckmann.hvkclient.ObserveAsEvents
import de.rafaelbeckmann.hvkclient.SnackbarController
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.ui.navigation.AppNavHost
import de.rafaelbeckmann.hvkclient.ui.navigation.OnboardingGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.navigation.VpGraph
import kotlinx.coroutines.launch


val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    settingsRepository: SettingsRepository,
    connectivityViewModel: ConnectivityViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

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

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                val showBottomBar = !isOnboarding && !isRevealMarkScreen

                // Connectivity state must be known before composing the bottom app bar
                val isConnected by connectivityViewModel.isConnected.collectAsStateWithLifecycle()
                val bannerVisible = !isConnected

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedVisibility(
                        visible = showBottomBar,
                        enter = expandVertically(
                            expandFrom = Alignment.Bottom,
                            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                        ) +
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                ),
                        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) +
                                slideOutVertically(targetOffsetY = { it })
                    ) {
                        // TODO: kann man das auch injecten?
                        AppBottomNavigation(
                            navController = navController,
                            currentDestination = currentDestination,
                            // Disable nav bar padding when the banner consumes it; otherwise keep it
                            windowInsets = if (bannerVisible)
                                WindowInsets(0.dp)
                            else
                                WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                        )
                    }


                    AnimatedVisibility(
                        visible = bannerVisible,
                        enter = expandVertically(
                            expandFrom = Alignment.Bottom,
                            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                        ) +
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                ),
                        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) +
                                slideOutVertically(targetOffsetY = { it })
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .fillMaxWidth()
                                .windowInsetsPadding(
                                    WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "kein Internet",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            )
        }
    }
}
