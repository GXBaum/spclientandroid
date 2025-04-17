package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.PrefUtils
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.CopyTokenButton
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, prefUtils: PrefUtils) {
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var isDeveloper by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            val savedUsername = prefUtils.getString("username")
            username = savedUsername ?: ""

            val savedIsDeveloper = prefUtils.getString("isDeveloper")
            isDeveloper = savedIsDeveloper.toBoolean()
        }
    }

    Column {
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

