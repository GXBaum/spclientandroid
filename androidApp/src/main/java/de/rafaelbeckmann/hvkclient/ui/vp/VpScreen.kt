package de.rafaelbeckmann.hvkclient.ui.vp

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rafaelbeckmann.hvkclient.features.vp.domain.SelectedCourse
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpDay
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpSubstitution
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpType
import de.rafaelbeckmann.hvkclient.features.vp.presentation.VpScreenState
import de.rafaelbeckmann.hvkclient.features.vp.presentation.VpViewModel
import de.rafaelbeckmann.hvkclient.relativeDateFormatter
import de.rafaelbeckmann.hvkclient.relativeDateTimeFormatter
import de.rafaelbeckmann.hvkclient.shared.resources.MR
import de.rafaelbeckmann.hvkclient.shared.resources.vp_add_course
import de.rafaelbeckmann.hvkclient.shared.resources.vp_deleted_entries_section_title
import de.rafaelbeckmann.hvkclient.shared.resources.vp_no_courses_selected
import de.rafaelbeckmann.hvkclient.shared.resources.vp_no_entries
import de.rafaelbeckmann.hvkclient.shared.resources.vp_open_webview
import de.rafaelbeckmann.hvkclient.shared.resources.vp_room_changes_section_title
import de.rafaelbeckmann.hvkclient.shared.resources.vp_schedule_in_progress
import de.rafaelbeckmann.hvkclient.shared.resources.vp_schedule_title
import de.rafaelbeckmann.hvkclient.shared.resources.vp_unverified_course_warning
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.common.HapticPullToRefreshBox
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
import de.rafaelbeckmann.hvkclient.ui.common.stringResource
import de.rafaelbeckmann.hvkclient.ui.main.LocalSnackbarHostState
import de.rafaelbeckmann.hvkclient.ui.navigation.VP_FAB_EXPLODE_BOUND
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun SharedTransitionScope.VpScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onVpOpenClick: (String?) -> Unit,
    onAddCourseClick: () -> Unit,
    modifier: Modifier = Modifier,
    course: String? = null,
    viewModel: VpViewModel = koinViewModel()
) {
    val state by viewModel.vpScreenState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current

    VpContent(
        modifier = modifier,
        state = state,
        course = course,
        onVpOpenClick = onVpOpenClick,
        onAddCourseClick = onAddCourseClick,
        onRefresh = { viewModel.refreshAsync() },
        fabModifier = Modifier
            .sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = VP_FAB_EXPLODE_BOUND
                ),
                animatedVisibilityScope = animatedVisibilityScope
            ),
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VpContent(
    modifier: Modifier = Modifier,
    fabModifier: Modifier = Modifier,
    state: VpScreenState,
    course: String? = null,
    onVpOpenClick: (String?) -> Unit = {},
    onAddCourseClick: () -> Unit,
    onRefresh: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.selectedCourses.size })

    var fabHeight by remember { mutableIntStateOf(0) }
    val fabHeightDp = with(LocalDensity.current) { fabHeight.toDp() }

    // Animate FAB collapse
    var fabExpanded by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(8.seconds)
        fabExpanded = false
    }


    // Safe index for composition to avoid TabRow crash when tabs shrink
    val pageCount = state.selectedCourses.size
    val lastIndex = (pageCount - 1).coerceAtLeast(0)
    val safeSelectedIndex by remember(lastIndex) {
        derivedStateOf { pagerState.currentPage.coerceIn(0, lastIndex) }
    }

    // Jump to tab from navigation arg once courses loaded
    LaunchedEffect(state.selectedCourses, course) {
        val target = course?.let { courseName -> state.selectedCourses.indexOfFirst { it.name == courseName } } ?: -1
        if (target >= 0) pagerState.scrollToPage(target.coerceAtMost(lastIndex))
    }

    // Correct pager when page count shrinks and current page is out-of-range
    LaunchedEffect(pageCount) {
        if (pagerState.currentPage > lastIndex) {
            pagerState.scrollToPage(lastIndex)
        }
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        modifier = modifier.safeDrawingPadding(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                expanded = fabExpanded,
                onClick = { onVpOpenClick(state.selectedCourses.getOrNull(safeSelectedIndex)?.name) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = stringResource(MR.strings.vp_open_webview)
                    )
                },
                text = { Text(stringResource(MR.strings.vp_schedule_title)) },
                modifier = fabModifier
                    .onGloballyPositioned { // calculate FAB height
                        fabHeight = it.size.height
                    }
                ,
            )
        }
    ) { innerPadding ->
        HapticPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.selectedCourses.isNotEmpty()) {

                    @Composable
                    fun TabsContent(){
                        state.selectedCourses.forEachIndexed { index, courseName ->
                            Tab(
                                selected = safeSelectedIndex == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(courseName.name) }
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

                    state.error?.let { ErrorCard(it) }


                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = fabHeightDp + 16.dp), // FAB height + Android hard-coded FAB offset
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            // non-existing course warning
                            state.selectedCourses.getOrNull(page)?.verified?.let {
                                if (!it) {
                                    item {
                                        ErrorCard(stringResource(MR.strings.vp_unverified_course_warning))
                                    }
                                }
                            }

                            val courseName = state.selectedCourses.getOrNull(page)

                            daySection("Heute", courseName, state.substitutions?.today)
                            daySection("Nächster Schultag", courseName, state.substitutions?.tomorrow)
                        }
                    }
                } else if (!state.isLoading) {
                    state.error?.let { ErrorCard(it) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(stringResource(MR.strings.vp_no_courses_selected))
                            Button(
                                onClick = onAddCourseClick
                            ) {
                                Text(stringResource(MR.strings.vp_add_course))
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    modifier: Modifier = Modifier,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        style = if (isHeader) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
        modifier = modifier
            .weight(weight)
            .padding(2.dp)
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
                    text = stringResource(MR.strings.vp_no_entries),
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    } else {
        vpTableItemsNotSureHowToNameThis(active)
    }

    if (deleted.isNotEmpty()){
        item {
            Text(stringResource(MR.strings.vp_deleted_entries_section_title))
        }
        vpTableItemsNotSureHowToNameThis(deleted, isDeleted = true, isDefaultExtended = true)
    }
}

fun LazyListScope.vpTableItemsNotSureHowToNameThis(
    items: List<VpSubstitution>,
    isDeleted: Boolean = false,
    isDefaultExtended: Boolean = false
) {
    // TODO: add deleted param
    roundedListItems(
        items = items,
        animatePlacement = false
        //key = { sub -> sub.id }
    ) { sub ->
        var isExtended by remember { mutableStateOf(isDefaultExtended) }
        Column (
            modifier = Modifier
                .clickable(
                    onClick = {
                        isExtended = !isExtended
                    }
                )
                .animateContentSize(
                    animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
                )
                .background(
                    color = if (isDeleted) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                )
        ){
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TableCell(text = sub.hour, weight = 0.075f)
                    TableCell(text = sub.original, weight = 0.25f)
                    TableCell(text = "→", weight = 0.075f)
                    TableCell(text = sub.replacement, weight = 0.25f)
                    TableCell(text = sub.description, weight = 0.35f)
                }

                if (isExtended) {
                    Text(
                        text = "zuerst gesehen: ${relativeDateTimeFormatter(sub.createdAt)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // if it's deleted the last change should be the deletion, so this should be fine
                    if (isDeleted) {
                        Text(
                            text = "gelöscht: ${relativeDateTimeFormatter(sub.updatedAt)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

fun LazyListScope.daySection(title: String, courseName: SelectedCourse?, list: VpDay?) {
    item(key = "header_${courseName}_$title") {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }

    // TODO: remove this, redundant
    list?.targetDate?.let {
        item {
            Text(
                relativeDateFormatter(it)
            )
        }
    }

    if (list?.dayString != null) {
        item(key = "header_day_${courseName}_$title") {
            Text(
                text = list.dayString,
                style = MaterialTheme.typography.bodyMediumEmphasized
            )
        }
    }

    val info = list?.info?.lastOrNull()?.text

    if (info?.isNotBlank() == true) {
        roundedListItems(
            items = listOf(info),
            animatePlacement = false
        ) {
            RoundedListItem(
                text = it
            )
        }
    }

    /*
    val filteredInfo = if (dayKey == "today") {
        state.substitutions?.today?.info?.map {
            VpInfoItem(
                data = it.text,
                summary = it.text,
                fetched_at = "FIXME_PLACEHOLDER", // FIXME THIS ABOMINATION
                id = 1,
                day = it.targetDate
            )
        }
    } else {
        state.substitutions?.tomorrow?.info?.map {
            VpInfoItem(
                data = it.text,
                summary = it.text,
                fetched_at = "FIXME_PLACEHOLDER",
                id = 1,
                day = it.targetDate
            )
        }
    }.orEmpty()

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
                    if(info.summary != info.data){
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
    */

    val substitutionsForCourse = list?.substitutions?.filter {
        it.courseName == courseName?.name &&
                it.VpType == VpType.substitution
    }
    val differentRoomsForCourse = list?.substitutions?.filter {
        it.courseName == courseName?.name &&
                it.VpType == VpType.differentRoom
    }

    if (list === null) {
        item { // duplicated from vpTableItems
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Text(
                    text = stringResource(MR.strings.vp_schedule_in_progress),
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }
    } else {
        vpTableItems(substitutionsForCourse.orEmpty())
    }

    if (!differentRoomsForCourse.isNullOrEmpty()) {
        item(key = "rooms_header_${courseName}_$title") {
            Text(
                text = stringResource(MR.strings.vp_room_changes_section_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        vpTableItems(differentRoomsForCourse)
    }
}

@Preview(showBackground = true)
@Composable
private fun VpEmptyPreview() {
    VpContent(
        state = VpScreenState(),
        onVpOpenClick = {},
        onAddCourseClick = {},
        onRefresh = {},
        snackbarHostState = SnackbarHostState()
    )
}
