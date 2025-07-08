package de.rafaelbeckmann.hvkclient.ui.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    val isDeveloper by viewModel.isDeveloper
    var username by viewModel.username
    val vpSelectedCourse by viewModel.vpSelectedCourse.collectAsState()
    var showAddCourseDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismissRequest = { showAddCourseDialog = false },
            onConfirm = { courseName ->
                if (courseName.isNotBlank()) {
                    viewModel.postVpSelectedCourse(courseName)
                    Log.d("SettingsScreen", "Neuer Kurs: $courseName")
                }
                showAddCourseDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Einstellungen",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(vertical = 8.dp)
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

        if (isDeveloper) {
            OutlinedButton(
                onClick = {
                    viewModel.resetOnboardingCompleted()
                },
            ) {
                Text(
                    text = "Onboarding Completed zurücksetzen",
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.clearCache(context)
                },
            ) {
                Text(
                    text = "Cache leeren",
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.deleteAccessToken()
                },
            ) {
                Text(
                    text = "Access Token löschen",
                )
            }
            OutlinedButton(
                onClick = {
                    viewModel.deleteRefreshToken()
                },
            ) {
                Text(
                    text = "Refresh Token löschen",
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Server Einstellungen",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        if (isDeveloper) {
            CopyTokenButton()
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(vpSelectedCourse) { course ->

                val isFirst = vpSelectedCourse.first() == course
                //val isLast = vpSelectedCourse.last() == course
                val shape = /*if (isFirst && isLast) {
                    RoundedCornerShape(16.dp)
                } else */if (isFirst) {
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp
                    )
                } /*else if (isLast) {
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                }*/ else {
                    RoundedCornerShape(4.dp)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = course,
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f)
                        )
                        IconButton(onClick = {
                            viewModel.deleteVpSelectedCourse(course)
                            Log.d("SettingsScreen", "Kurs löschen: $course")
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Kurs löschen"
                            )
                        }
                    }
                }
            }

            // TODO: das muss die absolut dümmste mögliche Lösung sein, aber ich weiß nicht wie ich es besser machen soll
            item {
                val shape = if (vpSelectedCourse.isEmpty()) {
                    RoundedCornerShape(16.dp)
                } else {
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .clickable(
                            onClick = {
                                showAddCourseDialog = true
                            }
                        ),
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // TODO: nur einen Button machen, der einen dann zu einem Dialog oder Screen führt, mit Suche
                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            modifier = Modifier.padding(16.dp),
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Kurs hinzufügen"
                        )
                    }
                }
            }
        }



    }
}

@Composable
private fun AddCourseDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var courseName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Kurs hinzufügen") },
        text = {
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Kursname (z.B. \"G10b\" oder \"E1/E2\"") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(courseName) }
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Abbrechen")
            }
        }
    )
}