package de.rafaelbeckmann.hvkclient.ui.vp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.ui.common.LoadingScreen

@Composable
fun VpScreen(
    modifier: Modifier = Modifier,
    viewModel: VpViewModel = hiltViewModel()
) {

    val isLoading = viewModel.isLoading.collectAsState()
    val error = viewModel.error
    val vpSelectedCourseName = viewModel.vpSelectedCourseName.value
    val vpSubstitutions = viewModel.vpSubstitutions.value

    val tomorrowVpSubstitutions = viewModel.tommorowSubstitutions.value

    Column (
        modifier = modifier
            .padding(horizontal = 8.dp)

    ) {
        if (isLoading.value) {
            LoadingScreen()
        } /*else if (error != null) {
            ErrorContent(error.toString())
        } */else {
            /*Text(
                text = "Heute",
                modifier = modifier
                    //.padding(8.dp)
            )*/
            VpTable(
                vpSubstitutions = vpSubstitutions
            )
            /*Text(
                text = "Morgen",
                modifier = modifier
                    //.padding(8.dp)
            )*/
            VpTable(
                vpSubstitutions = tomorrowVpSubstitutions
            )
        }


        Text(
            text = "ausgewählter Kurs: $vpSelectedCourseName",
            modifier = modifier
        )
    }


}

@Composable
fun VpTable(modifier: Modifier = Modifier,
            vpSubstitutions: List<VpSubstitution>
) {
    Row {
        LazyColumn(
            modifier = modifier
                .padding(2.dp)
        ) {
            items(vpSubstitutions) { substitution ->
                Text(text = substitution.hour)
            }
        }
        LazyColumn(
            modifier = modifier
                .padding(2.dp)
        ) {
            items(vpSubstitutions) { substitution ->
                Text(text = substitution.original)
            }
        }
        LazyColumn(
            modifier = modifier
                .padding(2.dp)
        ) {
            items(vpSubstitutions) { substitution ->
                Text(text = substitution.replacement)
            }
        }
        LazyColumn(
            modifier = modifier
                .padding(2.dp)
        ) {
            items(vpSubstitutions) { substitution ->
                Text(text = substitution.description)
            }
        }
    }
}
