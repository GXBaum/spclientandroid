package de.rafaelbeckmann.hvkclient.ui.settings

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.SnackbarController
import de.rafaelbeckmann.hvkclient.SnackbarEvent
import de.rafaelbeckmann.hvkclient.ui.common.CopyTokenButton
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

sealed class CourseListItem {
    data class Course(val name: String) : CourseListItem()
    object AddButton : CourseListItem()
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val isDeveloper by viewModel.isDeveloper
    val userId by viewModel.userId
    val vpSelectedCourse by viewModel.vpSelectedCourse.collectAsState()
    val courseSearch by viewModel.courseSearch.collectAsState()
    var showAddCourseDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var userIdString by remember { mutableStateOf(userId?.toString() ?: "") }

    if (showAddCourseDialog) {
        AddCourseDialog(
            onDismissRequest = { showAddCourseDialog = false },
            onConfirm = { courseName ->
                if (courseName.isNotBlank()) {
                    viewModel.postVpSelectedCourse(courseName)
                    Log.d("SettingsScreen", "Neuer Kurs: $courseName")
                }
                showAddCourseDialog = false
            },
            onQueryChanged = { q -> viewModel.searchCourses(q) },
            suggestions = courseSearch
        )
    }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        //verticalArrangement = Arrangement.spacedBy(8.dp) // TODO: eigentlich cool, aber macht die liste kaputt
    ) {
        item {
            Text(
                text = "Einstellungen",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { viewModel.toggleDeveloperMode(context) },
            )
        }

        if (isDeveloper) {
            item {
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
            }
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Benachrichtigungseinstellungen")
            }
        }

        item {
            Text(
                text = "Server Einstellungen",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isDeveloper) {
            item { CopyTokenButton() }
        }

        val courseListItems =
            vpSelectedCourse.map { CourseListItem.Course(it) } + CourseListItem.AddButton

        roundedListItems(
            items = courseListItems,
            key = { item ->
                when (item) {
                    is CourseListItem.Course -> "course_${item.name}"
                    is CourseListItem.AddButton -> "add_button"
                }
            },
            onItemClick = { item ->
                when (item) {
                    is CourseListItem.AddButton -> showAddCourseDialog = true
                    is CourseListItem.Course -> {}
                }
            }
        ) { item ->
            when (item) {
                is CourseListItem.AddButton -> {
                    RoundedListItem(
                        text = "Kurs hinzufügen",
                        trailingIcon = {
                            IconButton(onClick = {
                                showAddCourseDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Kurs hinzufügen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
                is CourseListItem.Course -> {
                    RoundedListItem(
                        text = item.name,
                        trailingIcon = {
                            IconButton(onClick = {
                                viewModel.deleteVpSelectedCourse(item.name)
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Kurs löschen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCourseDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
    onQueryChanged: (String) -> Unit,
    suggestions: List<String>
) {
    var courseName by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    /*LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }*/
    LaunchedEffect(focusRequester) {
        awaitFrame() // wait until dialog is there as the focus doesnt work TODO: check if a new version fixes it
        focusRequester.requestFocus()
    }

    /*ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {*/

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Kurs hinzufügen") },
        text = {
            Column {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = {
                        courseName = it
                        onQueryChanged(it)
                    },
                    label = { Text("Kursname (z.B. \"G10b\" oder \"E1/E2\"") },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirm(courseName) }
                    )
                )
                Spacer(Modifier.height(8.dp))
                if (suggestions.isNotEmpty()) {
                    Text(
                        "Vorschläge",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    LazyColumn {
                        roundedListItems(
                            items = suggestions,
                            key = { suggestion -> suggestion }
                        ) { suggestion ->
                            RoundedListItem(
                                text = suggestion,
                                modifier = Modifier.clickable {
                                    courseName = suggestion
                                    onConfirm(courseName)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(courseName) },
                enabled = courseName.isNotBlank()
            ) { Text("Hinzufügen") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Abbrechen")
            }
        }
    )/*}*/
}
