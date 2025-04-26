package de.rafaelbeckmann.hvkclient.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import de.rafaelbeckmann.hvkclient.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    navController: NavController,
    title: String = "HvK Client",
    onTitleClick: (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        modifier = if (onTitleClick != null) {
                            Modifier.clickable(onClick = onTitleClick)
                        } else {
                            Modifier
                        }
                    )
                },
                actions = {
                    Text(
                        text = "Einstellungen",
                        modifier = Modifier
                            .clickable {
                                navController.navigate(SettingsScreen)
                            }
                    )
                }
            )
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}