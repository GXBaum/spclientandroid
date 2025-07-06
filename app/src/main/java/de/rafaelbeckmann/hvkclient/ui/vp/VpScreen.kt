package de.rafaelbeckmann.hvkclient.ui.vp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpScreen(
    modifier: Modifier = Modifier,
    viewModel: VpViewModel = hiltViewModel()
) {
    val isLoading = viewModel.isLoading.collectAsState().value
    val error = viewModel.error.collectAsState()
    val vpSelectedCourseName = viewModel.vpSelectedCourse.collectAsState().value

    val vpSubstitutionsAll = viewModel.vpSubstitutionsAll.value

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = {
            if (vpSelectedCourseName.isNotEmpty()) {
                viewModel.fetchVpSubstitutionsAll(vpSelectedCourseName)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            // TODO: change this for a snack bar or something
            if (!error.value.isNullOrEmpty()) {
                item(key = "error_message") {
                    ErrorCard(error.value!!)
                }
            }

            // No substitutions message
            if (vpSubstitutionsAll?.substitutions?.flatten().isNullOrEmpty()) {
                item(key = "no_substitutions") {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Keine Vertretungen gefunden",
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Selected course
            if (vpSelectedCourseName.isNotEmpty()) {
                item(key = "selected_course") {
                    Text(
                        text = "Ausgewählter Kurs: $vpSelectedCourseName",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // All substitutions header
            if (vpSubstitutionsAll?.substitutions?.flatten()?.isNotEmpty() == true) {
                item(key = "all_substitutions_header") {
                    Text(
                        text = "Vertretungen",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Use efficient lazy loading for all substitutions
                itemsIndexed(vpSubstitutionsAll.substitutions.filter { it.isNotEmpty() }) { index, substitutionList ->
                    VpTable(
                        modifier = Modifier.padding(bottom = 8.dp),
                        vpSubstitutions = substitutionList
                    )
                }
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