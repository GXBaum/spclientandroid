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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VpScreen(
    modifier: Modifier = Modifier,
    viewModel: VpViewModel = hiltViewModel()
) {
    val state by viewModel.vpScreenState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = {
            if (state.selectedCourses.isNotEmpty()) {
                viewModel.fetchVpSubstitutionsMultipleCourses(state.selectedCourses)
            }
        },
        // indicator = { ContainedLoadingIndicator() }, // TODO: er ist oben links
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            if (state.selectedCourses.isNotEmpty()) {
                val pagerState = rememberPagerState { state.selectedCourses.size }
                LaunchedEffect(selectedTabIndex) {
                    pagerState.animateScrollToPage(selectedTabIndex)
                }
                LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                    if (!pagerState.isScrollInProgress) {
                        selectedTabIndex = pagerState.currentPage
                    }
                }

                PrimaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                    state.selectedCourses.forEachIndexed { index, courseName ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(courseName) }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val courseName = state.selectedCourses.getOrNull(page)
                    val substitutionsForCourse = courseName?.let { state.substitutions?.substitutions?.get(it) }
                    val allSubstitutions = substitutionsForCourse?.let { it.today + it.tomorrow } // TODO: WHAT

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (allSubstitutions.isNullOrEmpty()) {
                            item(key = "no_substitutions") {
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
                            item(key = "all_substitutions_header") {
                                Text(
                                    text = "Vertretungen",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            val substitutionGroups = listOfNotNull(
                                substitutionsForCourse.today.takeIf { it.isNotEmpty() },
                                substitutionsForCourse.tomorrow.takeIf { it.isNotEmpty() }
                            )
                            itemsIndexed(substitutionGroups) { _, substitutionList ->
                                VpTable(
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    vpSubstitutions = substitutionList
                                )
                            }
                        }
                    }
                }
            } else if (!state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Keine Kurse ausgewählt.")
                }
            }

            if (!state.error.isNullOrEmpty()) {
                ErrorCard(state.error!!)
            }
        }
    }
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