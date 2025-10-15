package de.rafaelbeckmann.hvkclient.ui.common

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.SnackbarController
import de.rafaelbeckmann.hvkclient.SnackbarEvent
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun DebugMenu(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDeveloper by viewModel.isDeveloper
    val userId by viewModel.userId
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userIdString by remember { mutableStateOf(userId?.toString() ?: "") }

    Column {
        Text (
            text = "nichts außer Leere...",
            modifier = Modifier
                .clickable { viewModel.toggleDeveloperMode(context) }
        )

        if (isDeveloper){
            OutlinedTextField(
                value = userIdString,
                onValueChange = { newValue ->
                    userIdString = newValue
                },
                label = { Text("SP User ID") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val parsedId = userIdString.toIntOrNull()
                            if (parsedId != null) {
                                viewModel.saveUsername(parsedId)
                                Toast.makeText(context, "User ID gespeichert", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ungültige User ID", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "ID speichern"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                OutlinedButton(
                    onClick = { viewModel.resetOnboardingCompleted() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Onboarding Completed zurücksetzen") }

                OutlinedButton(
                    onClick = { viewModel.clearCache(context) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cache leeren") }

                OutlinedButton(
                    onClick = { viewModel.deleteAccessToken() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Access Token löschen") }

                OutlinedButton(
                    onClick = { viewModel.deleteRefreshToken() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Refresh Token löschen") }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            SnackbarController.sendEvent(
                                event = SnackbarEvent(
                                    message = "Hello World!"
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Snackbar testen") }
            }
            CopyTokenButton()
        }
    }
}