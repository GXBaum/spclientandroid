package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.PrefUtils
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.CopyTokenButton
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.ErrorContent
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.LoadingScreen
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserCourse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    viewModel: CoursesViewModel = hiltViewModel(),
    onCourseClick: (UserCourse) -> Unit = {},
    prefUtils: PrefUtils,
) {
    val scope = rememberCoroutineScope()

    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var username by remember { mutableStateOf("") }


    // Fetch the username from SharedPreferences
    LaunchedEffect(Unit) {
        val savedUsername = prefUtils.getString("username")
        username = savedUsername.orEmpty()
    }


    // Use LaunchedEffect to fetch data when username changes
    // Use LaunchedEffect to fetch data only once when the screen is composed
    LaunchedEffect(username) {
        if (username.isNotEmpty()) {
            viewModel.fetchCourses(username)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        CopyTokenButton()

        when {
            isLoading -> {
                LoadingScreen()
            }
            error != null -> {
                ErrorContent(
                    errorMessage = error ?: "Unknown error occurred",
                )
            }

            courses.isNotEmpty() -> {
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.fetchCourses(username) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn {
                        items(courses) { course ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onCourseClick(course) }
                            ) {
                                Text(
                                    text = course.name + " (${course.courseId})" ,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}
