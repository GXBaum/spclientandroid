package de.rafaelbeckmann.hvkclient.ui.vp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
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
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
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

    // Animate FAB collapse
    var fabExpanded by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4_000)
        fabExpanded = false
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
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
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = {
                if (state.selectedCourses.isNotEmpty()) {
                    viewModel.fetchVpSubstitutionsMultipleCourses(state.selectedCourses)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = modifier.fillMaxSize()) {
                if (state.selectedCourses.isNotEmpty()) {

                    // Jump to tab from navigation arg once courses loaded
                    LaunchedEffect(state.selectedCourses, course) {
                        val index = course?.let { state.selectedCourses.indexOf(it) } ?: -1
                        if (index >= 0) pagerState.scrollToPage(index)
                    }

                    PrimaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                        state.selectedCourses.forEachIndexed { index, courseName ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = { Text(courseName) }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val courseName = state.selectedCourses.getOrNull(page)
                        val substitutionsForCourse = courseName?.let { state.substitutions?.substitutions?.get(it) }

                        val sections = listOf(
                            "Heute" to (substitutionsForCourse?.today.orEmpty()),
                            "Nächster Schultag" to (substitutionsForCourse?.tomorrow.orEmpty())
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            sections.forEach { (title, list) ->
                                item(key = "header_${courseName}_$title") {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(top = 16.dp)
                                    )
                                }

                                // TODO: horrible code, i think
                                val dayKey = when (title) {
                                    "Heute" -> "today"
                                    "Nächster Schultag" -> "tomorrow"
                                    else -> null
                                }
                                val filteredInfo = state.vpInfo!!.info.filter { it.day == dayKey }

                                roundedListItems(
                                    items = filteredInfo
                                ) { info ->
                                    Text(
                                        text = info.data,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }


                                vpTableItems(list)
                            }
                        }
                    }
                } else if (!state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Keine Kurse ausgewählt.")
                    }
                }

                state.error?.let { ErrorCard(it) }
            }
        }
    }
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


fun LazyListScope.vpTableItems(
    vpSubstitutions: List<VpSubstitution>
) {
    val (deleted, active) = vpSubstitutions.partition { it.isDeleted == 1 }

    if (vpSubstitutions.isEmpty()) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Keine Einträge",
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    } else {
        roundedListItems(
            items = active,
            //key = { sub -> sub.id }
        ) { sub ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                TableCell(text = sub.hour, weight = 0.10f)
                TableCell(text = sub.original, weight = 0.25f)
                TableCell(text = sub.replacement, weight = 0.25f)
                TableCell(text = sub.description, weight = 0.40f)
            }
        }
    }

    if (deleted.isNotEmpty()){
        item{
            Text("gelöscht")
        }
        roundedListItems(
            items = deleted,
        ) { sub ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                    )
                    .padding(8.dp)
            ) {
                TableCell(text = sub.hour, weight = 0.10f)
                TableCell(text = sub.original, weight = 0.25f)
                TableCell(text = sub.replacement, weight = 0.25f)
                TableCell(text = sub.description, weight = 0.40f)
            }
        }
    }
}
