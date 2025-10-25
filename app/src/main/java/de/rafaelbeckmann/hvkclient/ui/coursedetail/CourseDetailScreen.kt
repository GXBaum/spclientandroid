package de.rafaelbeckmann.hvkclient.ui.coursedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes.Companion.Cookie12Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: CourseDetailViewModel = hiltViewModel(),
    courseId: Int,
    onNavigateToRevealMark: (String) -> Unit = {}
) {
    val marks by viewModel.marks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var userId by remember { mutableStateOf<Int?>(null) }

    // Use LaunchedEffect to fetch data only once when the screen is composed
    LaunchedEffect(courseId) {
        // TODO: ist eigentlich ziemlich dumm gerade, aber kb zu ändern
        userId = viewModel.getUserId()
        userId?.let { id ->
            viewModel.fetchUserMarks(courseId, id)
        }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = {
            userId?.let { id ->
                viewModel.fetchUserMarks(courseId, id)
            }
        },
        modifier = Modifier
            .fillMaxSize()
        ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = WindowInsets.systemBars.asPaddingValues()
        ) {
            if (error != null) {
                item {
                    ErrorCard(error = error!!)
                }
            }

            if (marks.isNotEmpty()) {
                marksList(marks = marks, onMarkClick = { mark ->
                    onNavigateToRevealMark(mark.grade)
                })
            } else if (error == null && !isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keine Noten für diesen Kurs gefunden",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.marksList(
    marks: List<UserMark>,
    onMarkClick: (UserMark) -> Unit
) {
    val groupedMarks = marks.groupBy { it.half_year }

    item(key = "header") {
        Text(
            text = "Kursnoten",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    // TODO: manchmal falsche Reihenfolge
    groupedMarks.forEach { (halfYear, marksInGroup) ->
        item(key = "halfYear_$halfYear") {
            Text(
                text = "Halbjahr: $halfYear",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        markList(
            marksInGroup = marksInGroup.filter { mark -> !mark.isDeleted },
            onMarkClick = onMarkClick,
        )

        if (marksInGroup.any { mark -> mark.isDeleted }) {
            item {
                Text(
                    text = "gelöscht",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            markList(
                marksInGroup = marksInGroup.filter { mark -> mark.isDeleted },
                onMarkClick = onMarkClick,
                isDeleted = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.markList(
    marksInGroup: List<UserMark>,
    onMarkClick: (UserMark) -> Unit,
    isDeleted: Boolean? = false,
) {
    roundedListItems(
        items = marksInGroup,
        key = { it.id },
        onItemClick = onMarkClick
    ) { mark ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (isDeleted == true) it.background(MaterialTheme.colorScheme.errorContainer) else it }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (mark.name.isNotEmpty()) {
                    Text(
                        text = mark.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "kein Titel",
                        fontStyle = FontStyle.Italic
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Icon(
                        imageVector = Icons.Rounded.Today,
                        contentDescription = null,
                    )
                    Text(text = mark.date)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = Cookie12Sided.toShape()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mark.grade,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
