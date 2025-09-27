package de.rafaelbeckmann.hvkclient.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreenPage2(
    modifier: Modifier = Modifier,
    onLoginClicked: () -> Unit = {},
    onCreateAccountClicked: () -> Unit = {}
) {

    Column (
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ){
        Text("Page 2 of Onboarding")

        Spacer(
            Modifier.weight(1f)
        )

        Button(
            onClick = onLoginClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "einloggen",
            )
        }
        Button(
            onClick = { /* TODO: navigate to next page */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Account erstellen",
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen2Preview() {
    OnboardingScreenPage2()
}