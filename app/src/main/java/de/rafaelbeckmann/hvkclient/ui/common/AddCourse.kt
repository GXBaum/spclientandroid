package de.rafaelbeckmann.hvkclient.ui.common

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.ui.settings.SettingsViewModel
import kotlinx.coroutines.android.awaitFrame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val suggestions by viewModel.courseSearch.collectAsState()
    var courseName by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    fun onConfirm(courseName: String){
        if (courseName.isNotBlank()) {
            viewModel.postVpSelectedCourse(courseName)
            Log.d("SettingsScreen", "Neuer Kurs: $courseName")
        }
        onContinue()
    }

    /*LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }*/
    LaunchedEffect(focusRequester) {
        awaitFrame() // wait until dialog is there as the focus doesn't work TODO: check if a new version fixes it
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentWindowInsets = WindowInsets(0.dp),

        topBar = {
            TopAppBar(
                title = {
                    Text("Kurs hinzufügen")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onConfirm(courseName) },
                        enabled = courseName.isNotBlank()
                    ) {
                        Text("Hinzufügen")
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn (
            modifier = Modifier
                .padding(innerPadding),
        ){
            item{
                OutlinedTextField(
                    value = courseName,
                    onValueChange = {
                        courseName = it
                        viewModel.searchCourses(it)
                    },
                    label = { Text("Kursname (z.B. \"G10b\" oder \"E1/E2\"") },
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirm(courseName) }
                    )
                )
            }
            item{
                Spacer(Modifier.height(16.dp))
            }

            if (suggestions.isNotEmpty()) {
                item {
                    Text(
                        "Vorschläge",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    Spacer(Modifier.height(4.dp))

                }

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
}