package de.rafaelbeckmann.hvkclient.ui.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import de.rafaelbeckmann.hvkclient.ui.navigation.CoursesGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.NavItem
import de.rafaelbeckmann.hvkclient.ui.navigation.SettingsGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.VpGraph

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    currentDestination: NavDestination?,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets
) {
    FlexibleBottomAppBar(windowInsets = windowInsets) {
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
                            saveState = false // set both to true to preserve individual back stacks // TODO: will also pop when already on the screen
                        }
                        launchSingleTop = true
                        restoreState = false // set both to true to preserve individual back stacks
                    }
                }
            )
        }
    }
}