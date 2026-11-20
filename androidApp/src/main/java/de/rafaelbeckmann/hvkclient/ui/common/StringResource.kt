package de.rafaelbeckmann.hvkclient.ui.common

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.StringResource
import androidx.compose.ui.res.stringResource as androidStringResource

// helper to be able to omit .resourceId
@Composable
fun stringResource(resource: StringResource): String = androidStringResource(resource.resourceId)
