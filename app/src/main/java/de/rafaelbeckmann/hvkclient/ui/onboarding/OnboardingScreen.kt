package de.rafaelbeckmann.hvkclient.ui.onboarding

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NoAccounts
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch

// TODO: make screens into reusable component
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onContinueClicked: () -> Unit = {},
    onLoginClicked: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()

    var didNavigate by rememberSaveable{ mutableStateOf(false) }

    // Navigate after the account exists and userId is persisted.
    LaunchedEffect(loginState) {
        if (!didNavigate && loginState is LoginState.Success) {
            didNavigate = true
            onContinueClicked()
        }
    }

    Column(
        modifier = modifier
            //.padding(horizontal = 16.dp)
            .safeDrawingPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pages = listOf(
            OnboardingPageData("Vertretungsplan Benachrichtigungen", "Du kannst über ausgewählte Klassen benachrichtigt werden.", Icons.Rounded.Today),
            OnboardingPageData("Vertretungsplan Informationen", "Über die Infos oben auf dem Plan wirst du auch informiert.", Icons.Rounded.Info),
            OnboardingPageData("Kein Account notwendig", "Freiwillig kann einer für Schulportal Funktionen erstellt werden.", Icons.Rounded.NoAccounts),
        )

        val pagerState = rememberPagerState(pageCount = { pages.size })
        val coroutineScope = rememberCoroutineScope()

        // Centered content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "HvK Client",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Dies ist eine inoffizielle App für den Vertretungsplan und in der Zukunft auch teilweise das Schulportal.\n\nIn den allermeisten Fällen sollte die App richtig funktionieren, ich kann jedoch nichts garantieren.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(Modifier.height(32.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentPadding = PaddingValues(horizontal = 64.dp),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1
            ) { page ->
                OnboardingItem(
                    title = pages[page].title,
                    text = pages[page].text,
                    icon = pages[page].icon
                )
            }
        }

        // Indicators
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }

        Column(
            Modifier.padding(
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
        ){
            when (loginState) {
                is LoginState.Idle -> {
                    /* // FIXME: temporary fix for play store approval
                    OutlinedButton(
                        onClick = onLoginClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text("Ich habe einen Account")
                    }
                    */
                    Button(
                        onClick = {
                            // coroutine to call the suspend function
                            coroutineScope.launch {
                                val lastPageIndex = pagerState.pageCount - 1
                                if (pagerState.currentPage == lastPageIndex) {
                                    // Start creation; navigation happens on Success via LaunchedEffect.
                                    viewModel.createAccount()
                                } else {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "Weiter")
                    }
                }
                is LoginState.Loading -> {
                    LoadingIndicator()
                }
                is LoginState.Success -> {
                    Text("Setup erfolgreich!", color = MaterialTheme.colorScheme.primary)

                    Button(
                        onClick = {
                            onContinueClicked()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "Weiter")
                    }
                }
                is LoginState.Error -> {
                    val error = (loginState as LoginState.Error).message
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.createAccount() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Erneut versuchen")
                    }
                }
            }
        }

    }
}

data class OnboardingPageData(
    val title: String,
    val text: String? = null,
    val icon: ImageVector? = null
)

@Composable
fun OnboardingItem(
    modifier: Modifier = Modifier,
    title: String,
    text: String? = null,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp // Adds subtle shadow for card effect
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
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