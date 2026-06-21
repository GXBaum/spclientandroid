package de.rafaelbeckmann.hvkclient.ui.main

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import de.rafaelbeckmann.hvkclient.R
import de.rafaelbeckmann.hvkclient.ui.navigation.CoursesGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.NavGraphSpec
import de.rafaelbeckmann.hvkclient.ui.navigation.NavItem
import de.rafaelbeckmann.hvkclient.ui.navigation.SettingsGraph
import de.rafaelbeckmann.hvkclient.ui.navigation.TestGraph
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
            NavItem(
                "Vertretungen",
                ImageVector.vectorResource(id = R.drawable.home_24dp_e3e3e3_fill1_wght400_grad0_opsz24),
                ImageVector.vectorResource(id = R.drawable.home_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                0,
                VpGraph),
            NavItem(
                "SP Noten",
                ImageVector.vectorResource(id = R.drawable.star_24dp_e3e3e3_fill1_wght400_grad0_opsz24),
                ImageVector.vectorResource(id = R.drawable.star_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                0,
                CoursesGraph),
            NavItem(
                "Nachrichten",
                ImageVector.vectorResource(id = R.drawable.mail_24dp_e3e3e3_fill1_wght400_grad0_opsz24),
                ImageVector.vectorResource(id = R.drawable.mail_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                0,
                TestGraph
            ),
            NavItem(
                "Einstellungen",
                ImageVector.vectorResource(id = R.drawable.settings_24dp_e3e3e3_fill1_wght400_grad0_opsz24),
                ImageVector.vectorResource(id = R.drawable.settings_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                0,
                SettingsGraph
            )
        )

        navItemList.forEach { navItem ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(navItem.screenObject::class)
            } == true

            val isOnGraphStart = (navItem.screenObject as NavGraphSpec)
                .startDestination()
                .let { startRoute -> currentDestination?.hasRoute(startRoute::class) } == true

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
                        Icon(
                            imageVector = if (isSelected) navItem.selectedIcon else navItem.unselectedIcon,
                            contentDescription = navItem.label
                        )
                    }
                },
                label = { Text(navItem.label) },
                selected = isSelected,
                onClick = {
                    if (!isOnGraphStart) {
                        navController.navigate(navItem.screenObject) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false // set both to true to preserve individual back stacks
                            }
                            launchSingleTop = true
                            restoreState = false // set both to true to preserve individual back stacks
                        }
                    }
                }
            )
        }
    }
}