//@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.hvkclientmitbenachrichtigungen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.UserCourse


@Composable
fun CoursesScreen(modifier: Modifier = Modifier) {
    val viewModel: CoursesScreenViewModel = viewModel()
    val courses by viewModel.courses.collectAsState()

    // Add username handling
    val username = "Rafael.Beckmann" // Replace with actual username source

    CoursesList(
        courses = courses,
        onRefreshClick = { viewModel.fetchCourses(username) },
        modifier = modifier
    )
}

@Composable
fun CoursesList(
    courses: List<UserCourse>,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column {
        LazyColumn(modifier = modifier) {
            items(courses.size) { index ->
                Text(text = "${courses[index].name} (ID: ${courses[index].course_id})")
            }
        }

        Button(
            onClick = onRefreshClick,
            modifier = modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Text("Refresh Courses")
        }
    }
}