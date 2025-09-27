package de.rafaelbeckmann.hvkclient.ui.courses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    viewModel: CoursesViewModel = hiltViewModel(),
    onCourseClick: (UserCourse) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var userId by remember { mutableStateOf<Int?>(null) }
    var isDeveloper by remember { mutableStateOf(false) }

    // Use LaunchedEffect to fetch data only once when the screen is composed
    LaunchedEffect(Unit) {
        val savedUsername = viewModel.getUserId()
        userId = savedUsername
        userId?.let { viewModel.fetchCourses(it) }

        // TODO: warum ist das überhaupt da?
        val isDeveloper = viewModel.isDeveloper()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        //color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { userId?.let { viewModel.fetchCourses(it) } },
            modifier = Modifier.fillMaxSize(),
            // indicator = { ContainedLoadingIndicator() }, // TODO: das ist alles schwachsinn, ich gebe auf
        ) {
            LazyColumn(
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
                        key = { course -> course.courseId },
                        onItemClick = { course -> onCourseClick(course) }
                    ) { course ->
                        RoundedListItem(
                            text = course.name,
                            trailingIcon = {
                                if (isDeveloper) {
                                    Text(
                                        text = course.courseId.toString(),
                                        modifier = Modifier
                                            .padding(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}