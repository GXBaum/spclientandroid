package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.PrefUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, prefUtils: PrefUtils) {
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }


    Column {
        TextField(
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }



        Button(
            onClick = {
                scope.launch {
                    val savedUsername = prefUtils.getString("username")
                    username = savedUsername ?: "No username found"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Load")
        }
    }
}

