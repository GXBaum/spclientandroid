package de.rafaelbeckmann.hvkclient.ui.courses

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rafaelbeckmann.hvkclient.features.courses.domain.UserCourse
import de.rafaelbeckmann.hvkclient.features.courses.presentation.CoursesViewModel
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.common.HapticPullToRefreshBox
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.rememberSmartCollapseTopAppBarBehavior
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
import de.rafaelbeckmann.hvkclient.ui.main.LocalSnackbarHostState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    viewModel: CoursesViewModel = koinViewModel(),
    onCourseClick: (UserCourse) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var userId by remember { mutableStateOf<String?>(null) }
    var isDeveloper by remember { mutableStateOf(false) }

    // Use LaunchedEffect to fetch data only once when the screen is composed
    LaunchedEffect(Unit) {
        viewModel.refresh()

        isDeveloper = viewModel.isDeveloper()
    }

    val lazyColumnState = rememberLazyListState()
    val scrollBehavior = rememberSmartCollapseTopAppBarBehavior(lazyColumnState)

    Scaffold(
        snackbarHost = {
            val snackbarHostState = LocalSnackbarHostState.current
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        topBar = {
            val statusBarPadding = WindowInsets.systemBars.asPaddingValues()
            TopAppBar(
                title = { Text("Kurse") },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0.dp),
                contentPadding = PaddingValues(
                    top = statusBarPadding.calculateTopPadding()
                )
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        ) { innerPadding ->
        HapticPullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            // indicator = { ContainedLoadingIndicator() }, // TODO: das ist alles schwachsinn, ich gebe auf
        ) {
            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                uiState.error?.let { error ->
                    item {
                        ErrorCard(error = error)
                    }
                }

                if (uiState.courses.isNotEmpty()) {
                    roundedListItems(
                        items = uiState.courses,
                        key = { course -> course.id },
                        onItemClick = { course -> onCourseClick(course) }
                    ) { course ->
                        RoundedListItem(
                            text = course.name,
                            trailingIcon = {
                                if (isDeveloper) {
                                    Text(
                                        text = course.id.toString(),
                                        modifier = Modifier
                                            .padding(16.dp)
                                    )
                                }
                            }
                        )
                    }
                } else {
                    item{
                        Text("Hier gibt es noch nichts zu tun.")
                    }
                }
            }
        }
    }
}