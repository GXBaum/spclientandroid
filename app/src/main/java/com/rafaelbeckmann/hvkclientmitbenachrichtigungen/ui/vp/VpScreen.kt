package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.vp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.common.ErrorContent
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp

@Composable
fun VpScreen(
    modifier: Modifier = Modifier,
    viewModel: VpViewModel = hiltViewModel()
) {

    val isLoading = viewModel.isLoading
    val error = viewModel.error
    val vpSelectedCourseName = viewModel.vpSelectedCourseName.value
    val vpSubstitutions = viewModel.vpSubstitutions.value



    Column (
        modifier = modifier
            .padding(horizontal = 8.dp)

    ){
        /*
        Text(
            text = "Selected Course: $vpSelectedCourseName",
            modifier = modifier
        )*/

        Row {
            LazyColumn(
            ) {
                items(vpSubstitutions) { substitution ->
                    Text(text = substitution.hour)
                }
            }
            LazyColumn(
            ) {
                items(vpSubstitutions) { substitution ->
                    Text(text = substitution.original)
                }
            }
            LazyColumn(
            ) {
                items(vpSubstitutions) { substitution ->
                    Text(text = substitution.replacement)
                }
            }
            LazyColumn(
            ) {
                items(vpSubstitutions) { substitution ->
                    Text(text = substitution.description)
                }
            }
        }

        Text(
            text = "ausgewählter Kurs: $vpSelectedCourseName",
            modifier = modifier
        )
    }


}
