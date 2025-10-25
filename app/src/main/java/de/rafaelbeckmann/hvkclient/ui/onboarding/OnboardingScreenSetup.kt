package de.rafaelbeckmann.hvkclient.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.rafaelbeckmann.hvkclient.ui.common.AddCourseScreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreenSetup(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            Column(
                Modifier
                .imePadding() // keyboard padding
                .safeDrawingPadding()
                .padding(
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                )
            ) {
                OutlinedButton(
                    onClick = onContinueClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(MaterialTheme.colorScheme.surface)
                ) {
                    Text(text = "Vorerst überspringen")
                }
            }

        }
    ) { innerPadding ->
        AddCourseScreen(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            onBack = onBackClicked,
            onContinue = onContinueClicked
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen2Preview() {
    OnboardingScreenSetup()
}