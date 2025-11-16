package de.rafaelbeckmann.hvkclient.ui.settings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

@Composable
fun LibrariesScreen(modifier: Modifier = Modifier) {

    LibrariesContainer(
        libraries = produceLibraries().value,
        contentPadding = WindowInsets.systemBars.asPaddingValues()
    )
}
