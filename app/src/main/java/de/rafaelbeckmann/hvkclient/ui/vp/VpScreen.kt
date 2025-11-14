package de.rafaelbeckmann.hvkclient.ui.vp

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VpScreen(
//fun SharedTransitionScope.VpScreen(
    modifier: Modifier = Modifier,
    viewModel: VpViewModel = hiltViewModel(),
    course: String? = null,
    onVpOpenClick: () -> Unit,
    //animatedVisibilityScope: AnimatedVisibilityScope
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
                /*
                modifier = Modifier
                    .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = VP_FAB_EXPLODE_BOUND
                            ),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                */
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = {
                viewModel.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = modifier.fillMaxSize()) {
                if (state.selectedCourses.isNotEmpty()) {

                    // Safe index for composition to avoid TabRow crash when tabs shrink
                    val pageCount = state.selectedCourses.size
                    val lastIndex = (pageCount - 1).coerceAtLeast(0)
                    val safeSelectedIndex by remember {
                        derivedStateOf { pagerState.currentPage.coerceIn(0, lastIndex) }
                    }

                    // Jump to tab from navigation arg once courses loaded
                    LaunchedEffect(state.selectedCourses, course) {
                        val target = course?.let { state.selectedCourses.indexOf(it) } ?: -1
                        if (target >= 0) pagerState.scrollToPage(target.coerceAtMost(lastIndex))
                    }

                    // Correct pager when page count shrinks and current page is out-of-range
                    LaunchedEffect(pageCount) {
                        if (pagerState.currentPage > lastIndex) {
                            pagerState.scrollToPage(lastIndex)
                        }
                    }

                    @Composable
                    fun TabsContent(){
                        state.selectedCourses.forEachIndexed { index, courseName ->
                            Tab(
                                selected = safeSelectedIndex == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(courseName) }
                            )
                        }
                    }

                    // dynamic TabRow using clamped index
                    if (state.selectedCourses.size <= 4) {
                        PrimaryTabRow(selectedTabIndex = safeSelectedIndex) {
                            TabsContent()
                        }
                    } else {
                        PrimaryScrollableTabRow(selectedTabIndex = safeSelectedIndex) {
                            TabsContent()
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
                                val filteredInfo = state.vpInfo?.info
                                    ?.filter { it.day == dayKey }
                                    ?.filter { !(it.data.isNullOrBlank() && it.summary.isNullOrBlank()) }
                                    .orEmpty()

                                if (filteredInfo.isNotEmpty()){
                                    roundedListItems(
                                        items = filteredInfo,
                                        animatePlacement = false
                                    ) { info ->
                                        var isSummaryExpanded by rememberSaveable { mutableStateOf(false)}


                                        val infoText = if (isSummaryExpanded) {
                                            info.data.orEmpty()
                                        } else {
                                            info.summary.orEmpty()
                                        }

                                        RoundedListItem(
                                            text = infoText,
                                            trailingIcon = {
                                                val rotation by animateFloatAsState(
                                                    targetValue = if (isSummaryExpanded) 180f else 0f,
                                                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                                )
                                                IconButton(
                                                    onClick = { isSummaryExpanded = !isSummaryExpanded }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        modifier = Modifier.rotate(rotation)
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .clickable(
                                                    onClick = { isSummaryExpanded = !isSummaryExpanded }
                                                )
                                                .animateContentSize(
                                                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                                                )
                                                .background( color = MaterialTheme.colorScheme.surfaceVariant )
                                        )
                                    }
                                }

                                vpTableItems(list)

                                val roomsList = when (title) {
                                    "Heute" -> substitutionsForCourse?.roomsToday.orEmpty()
                                    "Nächster Schultag" -> substitutionsForCourse?.roomsTomorrow.orEmpty()
                                    else -> emptyList()
                                }
                                if (roomsList.isNotEmpty()){
                                    item(key = "rooms_header_${courseName}_$title") {
                                        Text(
                                            text = "Ersatzräume",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(top = 12.dp)
                                        )
                                    }
                                    vpTableItems(roomsList)
                                }
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
    val (deleted, active) = vpSubstitutions.partition { it.isDeleted }

    if (vpSubstitutions.isEmpty()) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    text = "Keine Einträge",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    } else {
        roundedListItems(
            items = active,
            animatePlacement = false
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
