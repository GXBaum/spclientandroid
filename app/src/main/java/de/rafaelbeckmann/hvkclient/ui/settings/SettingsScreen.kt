package de.rafaelbeckmann.hvkclient.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNotifications
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.rafaelbeckmann.hvkclient.features.vp.domain.SelectedCourse
import de.rafaelbeckmann.hvkclient.ui.common.DebugMenu
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.common.HapticPullToRefreshBox
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.rememberSmartCollapseTopAppBarBehavior
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
import de.rafaelbeckmann.hvkclient.ui.main.LocalSnackbarHostState

sealed class CourseListItem {
    data class Course(val course: SelectedCourse) : CourseListItem()
    object AddButton : CourseListItem()
}

sealed class SettingsItem {
    class Entry(
        val text: String,
        val icon: ImageVector,
        val onClick: () -> Unit
    ) : SettingsItem()

    class Switch(
        val text: String,
        val icon: ImageVector,
        val checked: Boolean?,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onAddCourseClick: () -> Unit = {},
    onLibrariesClick: () -> Unit = {}
) {
    val state by viewModel.settingsScreenState.collectAsState()

    val useDynamicColor by viewModel.useDynamicColor.collectAsState()

    var showWarning by remember { mutableStateOf(false) }

    val context = LocalContext.current



    // Get the PackageInfo object for the current application package
    val packageInfo = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val appVersion = remember(packageInfo) { packageInfo.versionName }
    val appVersionCode = remember(packageInfo) { packageInfo.longVersionCode.toString() }
    val androidSdkVersion = remember { Build.VERSION.SDK_INT }
    val device = remember { "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})" }

    fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            checkNotificationPermission(context)
        )
    }

    // TODO is LifecycleResumeEffect better?
    // Observe lifecycle to update permission on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = checkNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val shareEntries = remember(context, appVersion, appVersionCode, androidSdkVersion, device) {
        listOf(
            SettingsItem.Entry("Feedback", Icons.Rounded.Feedback) {
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
            SettingsItem.Entry("Diese App weiterempfehlen", Icons.Rounded.Share) {
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

    val lazyColumnState = rememberLazyListState()
    val scrollBehavior = rememberSmartCollapseTopAppBarBehavior(lazyColumnState)

    Scaffold(
        snackbarHost = {
            val snackbarHostState = LocalSnackbarHostState.current
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        topBar = {
            val statusBarPadding = WindowInsets.systemBars.asPaddingValues()
            TopAppBar(
                title = {
                    Text("Einstellungen")
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0.dp),
                contentPadding = PaddingValues(
                    top = statusBarPadding.calculateTopPadding()
                )
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->

        HapticPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.reload() },
            state = rememberPullToRefreshState(),
            modifier = Modifier
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = lazyColumnState,
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                //contentPadding = innerPadding
            ) {
                // TODO: das schöner machen (mit tippen zu den Einstellungen geleitet werden)
                item {
                    if (!hasNotificationPermission) {
                        ErrorCard(
                            "Benachrichtigungen sind nicht aktiviert."
                        )
                    }
                }

                item {
                    Spacer(Modifier.padding(vertical = 8.dp))
                }

                val courseListItems = state.vpSelectedCourse.map { CourseListItem.Course(it) } + CourseListItem.AddButton

                roundedListItems(
                    items = courseListItems,
                    key = { item ->
                        when (item) {
                            is CourseListItem.Course -> "course_${item.course}"
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
                            SettingsMenuItem(
                                SettingsItem.Entry(
                                    text = "Kurs hinzufügen",
                                    icon = Icons.Rounded.Add
                                ) {
                                    onAddCourseClick()
                                }
                            )
                        }

                        is CourseListItem.Course -> {
                            RoundedListItem(
                                text = item.course.name,
                                trailingIcon = {
                                    if (!item.course.verified && item.course.name != "_DEBUG") {
                                        IconButton(onClick = {
                                            showWarning = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Error,
                                                contentDescription = "Kursname konnte nicht bestätigt werden",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    IconButton(onClick = {
                                        viewModel.deleteVpSelectedCourse(item.course.id)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Kurs löschen",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                leadingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (!item.course.verified) {
                                                showWarning = true
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Class,
                                            tint = MaterialTheme.colorScheme.primary,
                                            contentDescription = null
                                        )
                                    }
                                },
                                modifier = Modifier.clickable(
                                    onClick = {
                                        if (!item.course.verified) {
                                            showWarning = true
                                        }
                                    }
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.padding(vertical = 8.dp))
                }

                val notificationSettingsEntries = listOf(
                    SettingsItem.Entry(
                        text = "Benachrichtigungseinstellungen",
                        icon = Icons.Rounded.EditNotifications
                    ) {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )

                settingsMenu(
                    entries = notificationSettingsEntries
                )


                // Adaptive Color on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    item {
                        Spacer(Modifier.padding(vertical = 8.dp))
                    }

                    roundedListItems(
                        items = listOf(
                            SettingsItem.Switch(
                                text = "Systemfarben verwenden",
                                icon = Icons.Rounded.ColorLens,
                                checked = useDynamicColor
                            ) { viewModel.toggleDynamicColor(it) }
                        )
                    ) {
                        SettingsSwitch(
                            text = it.text,
                            checked = it.checked,
                            onCheckedChange = it.onCheckedChange,
                            icon = it.icon
                        )
                    }
                }

                item {
                    Spacer(Modifier.padding(vertical = 8.dp))
                }

                settingsMenu(
                    entries = shareEntries
                )



                //if (vpSelectedCourse.any { it.name =="_DEBUG" }) {
                if (state.vpSelectedCourse.any { it.name == "_DEBUG" }) {
                    item {
                        DebugMenu()
                    }

                    /*
                    // TODO: Moritz hat gehatet und will es unbedingt entfernt haben.
                    // Open-Source-Lizenzen (nicht ganz sicher, was ich damit machen soll. eigentlich will ich es haben, aber es verwirrt diese uncs.)
                    val libraryEntries = listOf(
                        SettingsItem.Entry(
                            text = "Open-Source-Lizenzen",
                            icon = Icons.AutoMirrored.Rounded.LibraryBooks,
                            onClick = onLibrariesClick
                        )
                    )

                    item {
                        Spacer(Modifier.padding(vertical = 16.dp))
                    }

                    settingsMenu(
                        entries = libraryEntries
                    )
                    */
                }
            }
        }
    }

    // non-verified course warning
    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Klasse unbekannt",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Diese Klasse wurde noch nie im Vertretungsplan gefunden.\n\nAchte auf die richtige Schreibweise (genau wie im Vertretungsplan) und verwende am besten die vorgeschlagenen Klassen.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showWarning = false }
                ) {
                    Text("Schließen")
                }
            }
        )
    }
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean?,
    onCheckedChange: ((Boolean) -> Unit)?,
    icon: ImageVector? = null
) {
    RoundedListItem(
        modifier = modifier.clickable { checked?.let { onCheckedChange?.invoke(!it) } },
        text = text,
        leadingIcon = {
            icon?.let {
                IconButton(
                    onClick = { checked?.let { isChecked -> onCheckedChange?.invoke(!isChecked)}}
                ) {
                    Icon(
                        imageVector = it,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                }
            }
        },
        trailingIcon = {
            if (checked != null) {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    )
}

fun LazyListScope.settingsMenu(
    modifier: Modifier = Modifier,
    entries: List<SettingsItem.Entry>,
    key: ((SettingsItem.Entry) -> Any)? = null
) {
    // TODO: onItemClick doesn't make sense, right? should it just be deleted out of roundedListItems? // or was it because of the border radii
    roundedListItems(
        items = entries,
        key = key,
        onItemClick = { it.onClick() }
    ) { entry ->
        SettingsMenuItem(entry)
    }
}

@Composable
private fun SettingsMenuItem(
    entry: SettingsItem.Entry,
    modifier: Modifier = Modifier,
) {
    RoundedListItem(
        modifier = modifier,
        text = entry.text,
        leadingIcon = {
            IconButton(onClick = entry.onClick) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
