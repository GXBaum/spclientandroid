package de.rafaelbeckmann.hvkclient.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreenLogin(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
    onContinueClicked: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    val passwordState = remember { TextFieldState() }
    var showPassword by remember { mutableStateOf(false) }
    val loginState by viewModel.loginState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // TODO: change to OutlinedSecureTextField
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedSecureTextField(
            state = passwordState,
            label = { Text("Passwort") },
            textObfuscationMode =
                if (showPassword) {
                    TextObfuscationMode.Visible
                } else {
                    TextObfuscationMode.RevealLastTyped
                },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = {
                if (username.isNotBlank() && passwordState.text.isNotEmpty()) {
                    viewModel.login(username, passwordState.text.toString())
                    showPassword = false
                    focusManager.clearFocus()
                }
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = if (showPassword) "Passwort verbergen" else "Passwort anzeigen"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = loginState) {
            is LoginState.Idle -> {
                Button(
                    onClick = {
                        viewModel.login(username, passwordState.text.toString())
                        showPassword = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = username.isNotBlank() && passwordState.text.isNotEmpty()
                ) {
                    Text("Login")
                }
            }
            is LoginState.Loading -> {
                LoadingIndicator()
            }
            is LoginState.Success -> {
                onContinueClicked()

                Text("Login erfolgreich!", color = MaterialTheme.colorScheme.primary)

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
                    onClick = {
                        viewModel.login(username, passwordState.text.toString())
                        showPassword = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = username.isNotBlank() && passwordState.text.isNotEmpty()
                ) {
                    Text("Erneut versuchen")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen3Preview() {
    OnboardingScreenLogin()
}