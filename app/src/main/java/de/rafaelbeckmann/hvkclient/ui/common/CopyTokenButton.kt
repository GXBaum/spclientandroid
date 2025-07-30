package de.rafaelbeckmann.hvkclient.ui.common

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyTokenButton() {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            scope.launch {
                val localToken = Firebase.messaging.token.await()
                clipboardManager.setText(AnnotatedString(localToken))

                Toast.makeText(
                    context,
                    "Token kopiert!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    ) {
        Text("FCM Token kopieren")
    }
}