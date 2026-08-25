package de.rafaelbeckmann.hvkclient.ui.common

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rafaelbeckmann.hvkclient.SnackbarController
import de.rafaelbeckmann.hvkclient.SnackbarEvent
import de.rafaelbeckmann.hvkclient.UserPreferences
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebugMenu(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.settingsScreenState.collectAsStateWithLifecycle()

    val isDeveloper = state.isDeveloper
    val userId = state.userId
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userIdString by remember { mutableStateOf(userId ?: "") }

    Column {
        TextButton(
            onClick = {
                viewModel.toggleDeveloperMode()
                Toast.makeText(
                    context,
                    if (!isDeveloper) "Du bist jetzt im Debug Modus (No Diddy)" else "Du bist jetzt wieder im normalen Modus",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            Text (
                text = if(!isDeveloper) "nichts außer Leere..." else "Tippen, um den Entwicklermodus zu deaktivieren.",
            )
        }

        if (isDeveloper){
            OutlinedTextField(
                value = userIdString,
                onValueChange = { newValue ->
                    userIdString = newValue
                },
                label = { Text("User ID") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveUsername(userIdString)
                            Toast.makeText(context, "User ID gespeichert", Toast.LENGTH_SHORT).show()
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
                    onClick = {
                        viewModel.clearCache()
                        Toast.makeText(context, "Cache geleert hoffentlich", Toast.LENGTH_SHORT).show()
                    },
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





            TextButton(
                onClick = {
                    viewModel.getSpAuthCookieTest()
                }
            ) {
                Text("get token")
            }
            Text(state.spAuthTest.toString())

            TextButton(
                onClick = {
                    viewModel.getEncryptedUserPreferences()
                }
            ) {
                Text("load encrypted prefs")
            }
            Text(state.encryptedUserPreferences.toString())


            var spUsernameInput by remember { mutableStateOf("") }
            var spPasswordInput by remember { mutableStateOf("") }

            OutlinedTextField(
                value = spUsernameInput,
                onValueChange = { newValue ->
                    spUsernameInput = newValue
                },
                label = { Text("sp username") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            viewModel.setEncryptedUserPreferences(
                                UserPreferences(
                                    spUsername = spUsernameInput,
                                    spPassword = spPasswordInput
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "speichern"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = spPasswordInput,
                onValueChange = { newValue ->
                    spPasswordInput = newValue
                },
                label = { Text("sp password") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            viewModel.setEncryptedUserPreferences(
                                UserPreferences(
                                    spUsername = spUsernameInput,
                                    spPassword = spPasswordInput
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = "speichern"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )



            TextButton(
                onClick = {
                    viewModel.getSpTest()
                }
            ) {
                Text("get sp test")
            }
        }
    }
}