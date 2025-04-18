package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.coursedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.UserMark
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.ErrorContent
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.LoadingScreen

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

    // Use LaunchedEffect to fetch data only once when the screen is composed
    // TODO: remove hardcoded username
    LaunchedEffect(courseId) {
        viewModel.fetchUserMarks(courseId, "Rafael.Beckmann")
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorContent(errorMessage = error ?: "Unknown error occurred")
            marks.isNotEmpty() -> MarksList(marks = marks, onMarkClick = { mark ->
                onNavigateToRevealMark(mark.grade)
            })

            else -> Text(
                text = "No marks available for this course",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MarksList(marks: List<UserMark>,
              onMarkClick: (UserMark) -> Unit
) {
    LazyColumn(
    ) {
        item {
            Text(
                text = "Course Marks",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(marks) { mark ->
            MarkItem(mark = mark, onMarkClick = onMarkClick)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MarkItem(
    mark: UserMark,
    onMarkClick: (UserMark) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onMarkClick(mark) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = mark.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Date: ${mark.date}")
                Text(
                    text = "Grade: ${mark.grade}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Half Year: ${mark.half_year}")
        }
    }
}