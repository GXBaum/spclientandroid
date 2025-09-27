package de.rafaelbeckmann.hvkclient.ui.onboarding

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// TODO: make screens into reusable component
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit = {},
) {
    Column(
        modifier = modifier
            //.padding(horizontal = 16.dp)
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HvK Client",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Dies ist eine inoffizielle App für den Vertretungsplan und in der Zukunft auch teilweise das Schulportal. Keine Garantie für irgendetwas.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(Modifier.height(32.dp))

        val pages = listOf(
            OnboardingPageData("Vertretungsplan", "Beschreibung des Vertretungsplans hier."),
            OnboardingPageData("Schulportal", "Beschreibung des Schulportals hier."),
            OnboardingPageData("Schulportal Noten", "Beschreibung der Noten hier.")
        )

        val pagerState = rememberPagerState(pageCount = { pages.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Adjust height based on your content
            contentPadding = PaddingValues(horizontal = 64.dp), // Key: Adds peek space on sides
            pageSpacing = 16.dp,
            beyondViewportPageCount = 1
        ) { page ->
            OnboardingItem(
                title = pages[page].title,
                text = pages[page].text,
                image = pages[page].image // Pass Uri if you have images
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                //.align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(16.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onContinueClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Weiter")
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val text: String? = null,
    val image: Uri? = null
)

@Composable
fun OnboardingItem(
    modifier: Modifier = Modifier,
    title: String,
    text: String? = null,
    image: Uri? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(), // Scales to available space after padding
        shape = RoundedCornerShape(16.dp), // Softer corners like in screenshot
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp // Adds subtle shadow for card effect
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // If you have an image or icon (like the graph in the screenshot)
            if (image != null) {
                // Load image with Coil or similar: Image(painter = rememberAsyncImagePainter(image), contentDescription = null)
            } else {
                Icon(
                    imageVector = Icons.Rounded.Today, // Placeholder; replace with your graph or icon
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            if (text != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen()
}