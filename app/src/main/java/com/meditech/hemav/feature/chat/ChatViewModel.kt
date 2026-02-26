package com.meditech.hemav.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meditech.hemav.data.model.ChatMessage
import com.meditech.hemav.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasActiveAppointment: Boolean = false,
    val otherUserProfilePicUrl: String = "",
    val otherUserId: String = ""
)

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _chatsList = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val chatsList: StateFlow<List<Map<String, Any>>> = _chatsList.asStateFlow()

    private var currentChatId: String? = null

    val currentUserId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    fun loadUserChats() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val list = repository.getUserChats(uid)
            val enrichedList = list.map { chatMap ->
                val mutableChatMap = chatMap.toMutableMap()
                val participants = chatMap["participants"] as? List<String> ?: emptyList()
                val otherId = participants.firstOrNull { it != uid }
                if (otherId != null) {
                    try {
                        val doc = firestore.collection("users").document(otherId).get().await()
                        mutableChatMap["otherProfilePicUrl"] = doc.getString("profilePicUrl") ?: ""
                        mutableChatMap["otherName"] = doc.getString("name") ?: "User"
                    } catch (_: Exception) {}
                }
                mutableChatMap
            }
            _chatsList.value = enrichedList
        }
    }

    fun startListeningToChat(chatId: String) {
        currentChatId = chatId
        _uiState.value = _uiState.value.copy(isLoading = true)

        // Identify other user and fetch profile info
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val participants = chatId.split("_")
            val otherId = participants.firstOrNull { it != uid } ?: return@launch
            
            _uiState.value = _uiState.value.copy(otherUserId = otherId)
            
            try {
                val doc = firestore.collection("users").document(otherId).get().await()
                val picUrl = doc.getString("profilePicUrl") ?: ""
                _uiState.value = _uiState.value.copy(otherUserProfilePicUrl = picUrl)
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            repository.listenToMessages(chatId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
                .collect { messagesList ->
                    _uiState.value = _uiState.value.copy(
                        messages = messagesList,
                        isLoading = false,
                        error = null
                    )
                }
        }
        
        // Check for active appointments
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val participants = chatId.split("_")
            val otherId = participants.firstOrNull { it != uid } ?: return@launch
            
            val apptRepo = com.meditech.hemav.data.repository.AppointmentRepository()
            
            // Listen to appointments from both doctor and patient perspectives to be sure
            viewModelScope.launch {
                apptRepo.getAppointmentsForDoctor(uid).collect { doctorAppts ->
                    apptRepo.getAppointmentsForPatient(uid).collect { patientAppts ->
                        val allAppts = doctorAppts + patientAppts
                        val hasMatch = allAppts.any { 
                            (it.patientId == otherId || it.doctorId == otherId) && 
                            (it.status == com.meditech.hemav.data.model.AppointmentStatus.CONFIRMED || 
                             it.status == com.meditech.hemav.data.model.AppointmentStatus.PENDING)
                        }
                        _uiState.value = _uiState.value.copy(hasActiveAppointment = hasMatch)
                    }
                }
            }
        }
    }

    fun sendMessage(text: String, receiverId: String) {
        val chatId = currentChatId ?: return
        val uid = auth.currentUser?.uid ?: return

        val message = ChatMessage(
            text = text,
            senderId = uid,
            receiverId = receiverId,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.sendMessage(chatId, message)
            // The snapshot listener handles the UI update automatically when sendMessage completes and hits server.
            // Also refresh chat list metadata so the lastMessage updates
            loadUserChats()
        }
    }
}
