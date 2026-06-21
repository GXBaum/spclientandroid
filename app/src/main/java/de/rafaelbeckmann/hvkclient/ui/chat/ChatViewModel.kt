package de.rafaelbeckmann.hvkclient.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rafaelbeckmann.hvkclient.data.Resource
import de.rafaelbeckmann.hvkclient.data.model.Chat
import de.rafaelbeckmann.hvkclient.data.model.ChatMessage
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ChatState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    val chatMessages: List<ChatMessage> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: HvkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()


    fun getChats(userId: Int) {
        repository.getChats(userId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _chatState.value = _chatState.value.copy(
                        isLoading = true,
                        chats = result.data ?: _chatState.value.chats
                    )
                }
                is Resource.Success -> {
                    _chatState.value = _chatState.value.copy(
                        isLoading = false,
                        chats = result.data ?: _chatState.value.chats,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _chatState.value = _chatState.value.copy(
                        isLoading = false,
                        error = result.message,
                        chats = result.data ?: _chatState.value.chats
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    suspend fun getUserId(): Int? {
        return settingsRepository.getUserId()
    }


    fun getChatMessages(userId: Int, chatId: String) {
        repository.getChatMessages(userId, chatId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _chatState.value = _chatState.value.copy(
                        isLoading = true,
                        chatMessages = result.data ?: _chatState.value.chatMessages
                    )
                }
                is Resource.Success -> {
                    _chatState.value = _chatState.value.copy(
                        isLoading = false,
                        chatMessages = result.data ?: _chatState.value.chatMessages,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _chatState.value = _chatState.value.copy(
                        isLoading = false,
                        error = result.message,
                        chatMessages = result.data ?: _chatState.value.chatMessages
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun refreshChats(userId: Int) {
        getChats(userId)
    }

    fun refreshChatMessages(userId: Int, chatId: String) {
        getChatMessages(userId, chatId)
        getChats(userId) // TODO dumm, die op nachricht ist nicht in getChatMessages
    }

    fun sendReply(userId: Int, chatId: String, message: String) {
        viewModelScope.launch {
            repository.sendMessageReply(
                userId, chatId, message
            )
        }
    }
}