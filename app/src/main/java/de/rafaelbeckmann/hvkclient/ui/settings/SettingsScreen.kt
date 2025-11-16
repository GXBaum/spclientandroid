package de.rafaelbeckmann.hvkclient.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.ui.common.DebugMenu
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems

sealed class CourseListItem {
    data class Course(val name: String) : CourseListItem()
    object AddButton : CourseListItem()
}

// TODO: having both seems weird
class SettingsEntry(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
class SettingsSwitchEntry(
    val text: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onAddCourseClick: () -> Unit = {},
    onLibrariesClick: () -> Unit = {}
) {
    val vpSelectedCourse by viewModel.vpSelectedCourse.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()

    val context = LocalContext.current


    // Get the PackageInfo object for the current application package
    val packageInfo = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val appVersion = remember(packageInfo) { packageInfo.versionName }
    val appVersionCode = remember(packageInfo) { packageInfo.longVersionCode.toString() }
    val androidSdkVersion = remember { Build.VERSION.SDK_INT }
    val device = remember { "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})" }

    val entries = remember(context, appVersion, appVersionCode, androidSdkVersion, device) {
        listOf(
            SettingsEntry("Feedback", Icons.Rounded.Feedback) {
                val body =
                    "Version: $appVersion (versionCode: $appVersionCode)\nOS: Android SDK Version $androidSdkVersion\nGerät: $device\n\n--- Den oberen Text nicht löschen! ---\n\n\n"
                val subject = "App Feedback: "
                val mailto = "mailto:rafaelbeckmanndev@outlook.com" +
                        "?subject=${Uri.encode(subject)}" +
                        "&body=${Uri.encode(body)}"

                val intent = Intent(Intent.ACTION_SENDTO, mailto.toUri())
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        "Mail App auswählen:"
                    )
                )
            },
            SettingsEntry("Diese App weiterempfehlen", Icons.Rounded.Share) {
                val shareText =
                    "Benachrichtigungen für den Vertretungsplan. Jetzt im Play Store herunterladen.\n\nhttps://play.google.com/store/apps/details?id=de.rafaelbeckmann.hvkclient"
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }
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
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Spacer(Modifier.padding(vertical = 8.dp))
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
            Spacer(Modifier.padding(vertical = 8.dp))
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
                    is CourseListItem.AddButton -> onAddCourseClick()
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
                                onAddCourseClick()
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

        item {
            Spacer(Modifier.padding(vertical = 8.dp))
        }

        roundedListItems(
            items = entries,
            onItemClick = { it.onClick() }
        ) { item ->
            RoundedListItem(
                text = item.text,
                trailingIcon = {
                    IconButton( onClick = {
                        item.onClick()
                    }) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }

        item {
            Spacer(Modifier.padding(vertical = 16.dp))
        }

        val libraryEntries = listOf(
            SettingsEntry(
                text = "Bibliotheken",
                icon = Icons.AutoMirrored.Rounded.LibraryBooks,
                onClick = onLibrariesClick
            )
        )

        // TODO should be a composable
        roundedListItems(
            items = libraryEntries,
            onItemClick = { it.onClick() }
        ) { item ->
            RoundedListItem(
                text = item.text,
                trailingIcon = {
                    IconButton(onClick = {
                        item.onClick()
                    }) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }

        if (vpSelectedCourse.contains("_DEBUG")) {
            item {
                DebugMenu()
            }

            // Adaptive Color on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                roundedListItems(
                    items = listOf(
                        SettingsSwitchEntry(
                            text = "Systemfarben verwenden",
                            checked = useDynamicColor
                        ) { it -> viewModel.toggleDynamicColor(it) }
                    )
                ) {
                    SettingsSwitch(
                        text = it.text,
                        checked = it.checked,
                        onCheckedChange = it.onCheckedChange
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    RoundedListItem(
        modifier = modifier.clickable { onCheckedChange?.invoke(!checked) },
        text = text,
        trailingIcon = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    )
}
