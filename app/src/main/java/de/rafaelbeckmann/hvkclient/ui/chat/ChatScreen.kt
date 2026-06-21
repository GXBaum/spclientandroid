package de.rafaelbeckmann.hvkclient.ui.chat

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.ui.common.HapticPullToRefreshBox
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.rememberSmartCollapseTopAppBarBehavior
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
import de.rafaelbeckmann.hvkclient.ui.main.LocalSnackbarHostState

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
    onChatClick: (chatId: String) -> Unit
) {
    val state by viewModel.chatState.collectAsState()
    var userId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(true){
        userId = viewModel.getUserId()
        userId?.let {
            viewModel.refreshChats(it)
        }
    }

    val lazyColumnState = rememberLazyListState()
    val scrollBehavior = rememberSmartCollapseTopAppBarBehavior(lazyColumnState)

    Scaffold(
        snackbarHost = {
            val snackbarHostState = LocalSnackbarHostState.current
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            val statusBarPadding = WindowInsets.systemBars.asPaddingValues()
            TopAppBar(
                title = { Text("Nachrichten Test") },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0.dp),
                contentPadding = PaddingValues(
                    top = statusBarPadding.calculateTopPadding()
                )
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        HapticPullToRefreshBox(
            onRefresh = {
                userId?.let {
                    viewModel.refreshChats(it)
                }
            },
            isRefreshing = state.isLoading,
            modifier = Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                roundedListItems(
                    items = state.chats,
                    onItemClick = { chat ->
                        onChatClick(chat.chatId)
                    }
                ) { chat ->
                    RoundedListItem(
                        text = chat.betreff
                    )
                }
            }
        }
    }
}