package de.rafaelbeckmann.hvkclient.ui.vp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import kotlinx.coroutines.launch

/*
     TODO: this crashes when the user deletes a course in settings and then goes back to the vp screen:
    FATAL EXCEPTION: main
    Process: de.rafaelbeckmann.hvkclient, PID: 23761
    java.lang.IndexOutOfBoundsException: Index 14 out of bounds for length 14
*/
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VpScreen(
    modifier: Modifier = Modifier,
    viewModel: VpViewModel = hiltViewModel(),
    course: String? = null,
    onVpOpenClick: () -> Unit
) {
    val state by viewModel.vpScreenState.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.selectedCourses.size })

    // animate Fab
    var fabExpanded by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4_000)
        fabExpanded = false
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                expanded = fabExpanded,
                onClick = { onVpOpenClick() },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = "Vertretungsplan öffnen"
                    )
                },
                text = { Text("Vertretungsplan") },
            )
        },
        content = { innerPadding ->
            val nothing = innerPadding // TODO: temporary fix to use innerPadding

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = {
                    if (state.selectedCourses.isNotEmpty()) {
                        viewModel.fetchVpSubstitutionsMultipleCourses(state.selectedCourses)
                    }
                },
                // indicator = { ContainedLoadingIndicator() }, // TODO: er ist oben links
                modifier = Modifier
                    .fillMaxSize()
                    //.padding(innerPadding), // todo fix
            ) {
                Column(modifier = modifier.fillMaxSize()) {
                    if (state.selectedCourses.isNotEmpty()) {

                        // Jump to the tab from the navigation argument once courses are loaded.
                        LaunchedEffect(state.selectedCourses, course) {
                            val index = course?.let { state.selectedCourses.indexOf(it) } ?: -1
                            if (index >= 0) {
                                pagerState.scrollToPage(index)
                            }
                        }

                        PrimaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                            state.selectedCourses.forEachIndexed { index, courseName ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(courseName) }
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            val courseName = state.selectedCourses.getOrNull(page)
                            val substitutionsForCourse = courseName?.let { state.substitutions?.substitutions?.get(it) }

                            val sections = buildList {
                                val today = substitutionsForCourse?.today.orEmpty()
                                val tomorrow = substitutionsForCourse?.tomorrow.orEmpty()
                                if (today.isNotEmpty()) add("Heute" to today)
                                if (tomorrow.isNotEmpty()) add("Nächster Schultag" to tomorrow)
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                if (sections.isEmpty()) {
                                    item(key = "no_substitutions_$courseName") {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Text(
                                                text = "Keine Vertretungen gefunden",
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .align(Alignment.CenterHorizontally)
                                            )
                                        }
                                    }
                                } else {
                                    sections.forEach { (title, list) ->
                                        item(
                                            key = "header_${courseName}_$title",
                                        ){
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                        item(key = "table_${courseName}_$title") {
                                            VpTable(
                                                modifier = Modifier.padding(bottom = 8.dp),
                                                vpSubstitutions = list
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!state.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Keine Kurse ausgewählt.")
                        }
                    }

                    state.error?.let { ErrorCard(it) }
                }
            }
        }
    )
}

@Composable
fun VpTable(
    modifier: Modifier = Modifier,
    vpSubstitutions: List<VpSubstitution>
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                TableCell(text = "Stunde", weight = 0.15f, isHeader = true)
                TableCell(text = "Original", weight = 0.25f, isHeader = true)
                TableCell(text = "Vertretung", weight = 0.25f, isHeader = true)
                TableCell(text = "Hinweis", weight = 0.35f, isHeader = true)
            }

            // Table Content
            vpSubstitutions.forEachIndexed { index, substitution ->
                TableRow(index, substitution)
            }
        }
    }
}

@Composable
private fun TableRow(index: Int, substitution: VpSubstitution) {
    val background = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(8.dp)
    ) {
        TableCell(text = substitution.hour, weight = 0.15f)
        TableCell(text = substitution.original, weight = 0.25f)
        TableCell(text = substitution.replacement, weight = 0.25f)
        TableCell(text = substitution.description, weight = 0.35f)
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        style = if (isHeader) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .weight(weight)
            .padding(4.dp)
    )
}
