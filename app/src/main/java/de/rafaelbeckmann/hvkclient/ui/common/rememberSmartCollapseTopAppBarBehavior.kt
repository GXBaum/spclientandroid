package de.rafaelbeckmann.hvkclient.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable

// TODO: in combination with .animatePlacement the LazyColumn items will YEAT themselves when the column length is just on the edge of fitting
@Composable
fun rememberSmartCollapseTopAppBarBehavior(
    lazyColumnState: LazyListState,
    topAppBarState: TopAppBarState = rememberTopAppBarState()
): TopAppBarScrollBehavior {
    return TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState,
        canScroll = {
            topAppBarState.collapsedFraction > 0f ||
                    lazyColumnState.canScrollForward ||
                    lazyColumnState.canScrollBackward
        }
    )
}