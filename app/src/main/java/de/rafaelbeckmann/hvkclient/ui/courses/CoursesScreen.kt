package de.rafaelbeckmann.hvkclient.ui.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    viewModel: CoursesViewModel = hiltViewModel(),
    onCourseClick: (UserCourse) -> Unit = {},
) {
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var username by remember { mutableStateOf("") }
    var isDeveloper by remember { mutableStateOf(false) }

    // Use the PrefUtils instance from the ViewModel
    val prefUtils = viewModel.prefUtils

    // Use LaunchedEffect to fetch data only once when the screen is composed
    LaunchedEffect(Unit) {
        val savedUsername = prefUtils.getString("username")
        username = savedUsername.orEmpty()
        if (username.isNotEmpty()) {
            viewModel.fetchCourses(username)
        }

        val savedIsDeveloper = prefUtils.getString("isDeveloper")
        isDeveloper = savedIsDeveloper == "true"
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        //color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchCourses(username) },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                if (error != null) {
                    ErrorCard(error = error!!)
                }

                if (courses.isNotEmpty()) {
                    // TODO: vielleicht zu einem eigenen Composable machen
                    LazyColumn {
                        items(courses) { course ->

                            val isFirst = courses.first() == course
                            val isLast = courses.last() == course
                            val shape = if (isFirst && isLast) {
                                RoundedCornerShape(16.dp)
                            } else if (isFirst) {
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 4.dp
                                )
                            } else if (isLast) {
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                            } else {
                                RoundedCornerShape(4.dp)
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                                    .clickable { onCourseClick(course) },
                                shape = shape,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                            ) {
                                Row {
                                    Text(
                                        text = course.name,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    if (isDeveloper) {
                                        Spacer(
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${course.courseId}",
                                            modifier = Modifier
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}