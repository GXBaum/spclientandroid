package de.rafaelbeckmann.hvkclient.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.w3c.dom.Text

@Preview
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit = {},
) {
    //Image()

    Column (
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ){
        Text(
            text = "HvK Client",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Dies ist eine inoffizielle App für den Vertretungsplan und in der Zukunft auch teilweise das Schulportal. Keine Garantie für irgendetwas, viele Sachen sind sehr unsicher."
        )

        Button(
            onClick = onContinueClicked
        ) {
            Text(
                text = "Weiter"
            )
        }
    }

}

@Composable
fun OnboardingItem(
    title: Text,

    modifier: Modifier = Modifier
) {

}