package de.rafaelbeckmann.hvkclient.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.ui.common.CopyTokenButton

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()

    val isDeveloper by viewModel.isDeveloper
    var username by viewModel.username
    val vpSelectedCourse = viewModel.vpSelectedCourse.collectAsState()
    var vpCoursenameInput by remember { mutableStateOf("") }

    val context = LocalContext.current



    Column (
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Einstellungen",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
                .clickable(
                    onClick = {
                        viewModel.toggleDeveloperMode(context)
                    }
                ),
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("SP Benutzername") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.saveUsername(username)
            },
        ) {
            Text("Speichern")
        }

        Text(
            text = "Server Einstellungen",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        if (isDeveloper) {
            CopyTokenButton()
        }


        OutlinedTextField(
            value = vpCoursenameInput,
            onValueChange = {vpCoursenameInput = it },
            label = { Text("Vertretungsplankurs") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Vertretungsplankurs: ${vpSelectedCourse.value}",
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.postVpSelectedCourse(vpCoursenameInput)
            },
        ) {
            Text("Speichern")
        }



    }
}

