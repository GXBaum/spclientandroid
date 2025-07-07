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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.ui.navigation.AppNavHost
import de.rafaelbeckmann.hvkclient.ui.navigation.CoursesGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.NavItem
import de.rafaelbeckmann.hvkclient.ui.navigation.OnboardingGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.RevealMarkScreen
import de.rafaelbeckmann.hvkclient.ui.navigation.SettingsGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.VpGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.VpScreen
import de.rafaelbeckmann.hvkclient.ui.theme.HvKClientTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var prefUtils: PrefUtils

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()

        setContent {
            HvKClientTheme {
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
                            FlexibleBottomAppBar {
                                // TODO: implement notification badge count
                                val navItemList = listOf(
                                    NavItem("Vertretungsplan", Icons.Rounded.Home, 0, VpGraph),
                                    NavItem("SP Noten", Icons.Rounded.Grade, 0, CoursesGraph),
                                    NavItem("Einstellungen", Icons.Rounded.Settings, 0, SettingsGraph)
                                )

                                navItemList.forEach { navItem ->
                                    val isSelected = currentDestination?.hierarchy?.any {
                                        it.hasRoute(navItem.screenObject::class)
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
                    }
                ) { innerPadding ->
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