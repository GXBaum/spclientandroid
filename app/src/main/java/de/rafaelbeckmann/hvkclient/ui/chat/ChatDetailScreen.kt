package de.rafaelbeckmann.hvkclient.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.rafaelbeckmann.hvkclient.ui.common.HapticPullToRefreshBox
import de.rafaelbeckmann.hvkclient.ui.common.RoundedListItem
import de.rafaelbeckmann.hvkclient.ui.common.rememberSmartCollapseTopAppBarBehavior
import de.rafaelbeckmann.hvkclient.ui.common.roundedListItems
import de.rafaelbeckmann.hvkclient.ui.main.LocalSnackbarHostState
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
    chatId: String
) {
    val state by viewModel.chatState.collectAsState()
    var userId by remember { mutableStateOf<Int?>(null) }

    var messageInput by remember { mutableStateOf("") }

    var fabHeight by remember { mutableIntStateOf(0) }
    val fabHeightDp = with(LocalDensity.current) { fabHeight.toDp() }


    LaunchedEffect(chatId){
        userId = viewModel.getUserId()
        userId?.let {
            viewModel.refreshChatMessages(it, chatId)
        }
    }

    val density = LocalDensity.current

    // calculate if keyboard is visible to change textfieldsize
    val imeBottom = WindowInsets.ime.getBottom(density)
    val keyboardVisible = imeBottom > 0
    val textFieldPadding by animateDpAsState(
        if (keyboardVisible) 8.dp else 16.dp,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
    )


    val scope = rememberCoroutineScope()

    val lazyColumnState = rememberLazyListState()
    val scrollBehavior = rememberSmartCollapseTopAppBarBehavior(lazyColumnState)

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = lazyColumnState.layoutInfo

            val count = layoutInfo.totalItemsCount
            if (count == 0) return@derivedStateOf true
            val currentVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            currentVisibleIndex == count - 1
        }
    }

    Scaffold(
        snackbarHost = {
            val snackbarHostState = LocalSnackbarHostState.current
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            val statusBarPadding = WindowInsets.systemBars.asPaddingValues()
            TopAppBar(
                title = { state.chats.find { it.chatId == chatId }?.betreff?.let { Text( it) } },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0.dp),
                contentPadding = PaddingValues(
                    top = statusBarPadding.calculateTopPadding()
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .imePadding()
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                TextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            // apply padding to left, right and bottom, but not top to make the FAB distance smaller
                            start = textFieldPadding,
                            end = textFieldPadding,
                            bottom = textFieldPadding
                        )
                    ,
                    placeholder = {
                        Text("Message")
                    },
                    shape = RoundedCornerShape(32.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    maxLines = 7,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                userId?.let {
                                    viewModel.sendReply(
                                        it,
                                        chatId,
                                        messageInput,
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .onGloballyPositioned { // calculate FAB height
                        fabHeight = it.size.height
                    }
            ) {
                AnimatedVisibility(
                    visible = !isAtBottom,
                    enter = scaleIn(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    ),
                    exit = scaleOut(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                    )
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                lazyColumnState.animateScrollToItem(
                                    (state.chatMessages.size - 1).coerceAtLeast(0)
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                        /*modifier = Modifier.alpha(
                        animateFloatAsState(
                            if (isAtBottom) 1f else 0f
                        ).value
                    )*/
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null
                        )
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text("antworten")
                    }
                )
            }
        },
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        HapticPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = {
                userId?.let {
                    viewModel.refreshChatMessages(it, chatId)
                }
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            LazyColumn(
                state = lazyColumnState,
                contentPadding = PaddingValues(bottom = fabHeightDp + 16.dp), // FAB height + Android hard-coded FAB offset
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                roundedListItems(
                    items = listOf(
                        state.chats.find { it.chatId == chatId }?.content ?: ""
                    )
                ) {
                    RoundedListItem(
                        text = it
                    )
                }

                roundedListItems(
                    items = state.chatMessages,
                ) { message ->
                    RoundedListItem(
                        text = message.content
                    )
                }
            }
        }
    }
}