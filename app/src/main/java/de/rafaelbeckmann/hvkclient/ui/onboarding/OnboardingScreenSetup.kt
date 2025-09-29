package de.rafaelbeckmann.hvkclient.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreenSetup(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(),

    ) {
    val loginState by viewModel.loginState.collectAsState()

    var isNotificationEnabled by rememberSaveable { mutableStateOf(true) }

    Column (
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
            Spacer(modifier = Modifier.weight(1f)) // bottom spacer to center toggle


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isNotificationEnabled = !isNotificationEnabled }
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Benachrichtigungen serverseitig aktivieren",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isNotificationEnabled,
                onCheckedChange = { isNotificationEnabled = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        
        Column(
            Modifier.padding(
                bottom = 16.dp,
                //start = 16.dp,
                //end = 16.dp,
            )
        ){
            when (val state = loginState) {
                is LoginState.Idle -> {
                    Button(
                        onClick = { viewModel.createAccount(if (isNotificationEnabled) 1 else 0) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Weiter")
                    }
                }
                is LoginState.Loading -> {
                    LoadingIndicator()
                }
                is LoginState.Success -> {
                    Text("Setup erfolgreich!", color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onContinueClicked() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Weiter")
                    }
                }
                is LoginState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.createAccount(if (isNotificationEnabled) 1 else 0) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Erneut versuchen")
                    }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen2Preview() {
    OnboardingScreenSetup()
}