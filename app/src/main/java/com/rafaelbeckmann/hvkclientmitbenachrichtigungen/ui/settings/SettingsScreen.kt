package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.CopyTokenButton
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var isDeveloper by remember { mutableStateOf(false) }

    // Use the PrefUtils instance from the ViewModel
    val prefUtils = viewModel.prefUtils

    LaunchedEffect(Unit) {
        scope.launch {
            val savedUsername = prefUtils.getString("username")
            username = savedUsername ?: ""

            val savedIsDeveloper = prefUtils.getString("isDeveloper")
            isDeveloper = savedIsDeveloper.toBoolean()
        }
    }

    Column (
        modifier = modifier
            .padding(horizontal = 8.dp)
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    prefUtils.saveString("username", username)
                }
            },
        ) {
            Text("Save")
        }

        if (isDeveloper) {
            CopyTokenButton()
        }

    }
}

