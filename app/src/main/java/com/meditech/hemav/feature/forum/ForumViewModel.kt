package com.meditech.hemav.feature.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meditech.hemav.data.model.ForumPost
import com.meditech.hemav.data.model.ForumReply
import com.meditech.hemav.data.repository.ForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ForumUiState(
    val posts: List<ForumPost> = emptyList(),
    val currentReplies: List<ForumReply> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ForumViewModel : ViewModel() {
    private val repository = ForumRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(ForumUiState())
    val uiState: StateFlow<ForumUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    private fun loadPosts() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.getAllPosts()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
                .collect { posts ->
                    _uiState.value = _uiState.value.copy(posts = posts, isLoading = false)
                }
        }
    }

    fun loadReplies(postId: String) {
        viewModelScope.launch {
            repository.getPostReplies(postId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
                .collect { replies ->
                    _uiState.value = _uiState.value.copy(currentReplies = replies)
                }
        }
    }

    fun createPost(title: String, content: String, tags: List<String>, isDoctor: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid).get().await()
            val name = userDoc.getString("name") ?: auth.currentUser?.displayName ?: "User"
            val picUrl = userDoc.getString("profilePicUrl") ?: ""

            val post = ForumPost(
                title = title,
                content = content,
                authorId = uid,
                authorName = name,
                authorProfilePicUrl = picUrl,
                authorRole = if (isDoctor) "doctor" else "patient",
                isDoctorVerified = isDoctor,
                tags = tags
            )
            repository.createPost(post)
        }
    }

    fun addReply(postId: String, content: String, isDoctor: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid).get().await()
            val name = userDoc.getString("name") ?: auth.currentUser?.displayName ?: "User"
            val picUrl = userDoc.getString("profilePicUrl") ?: ""

            val reply = ForumReply(
                postId = postId,
                content = content,
                authorId = uid,
                authorName = name,
                authorProfilePicUrl = picUrl,
                authorRole = if (isDoctor) "doctor" else "patient",
                isDoctorVerified = isDoctor
            )
            repository.addReply(reply)
        }
    }

    fun upvotePost(postId: String) {
        viewModelScope.launch {
            repository.upvotePost(postId)
        }
    }
}
