package com.meditech.hemav.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.meditech.hemav.data.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val chatsCollection = firestore.collection("chats")

    /**
     * Generate a consistent chat ID between two users
     */
    fun getChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}"
        else "${userId2}_${userId1}"
    }

    /**
     * Send a message in a chat
     */
    suspend fun sendMessage(chatId: String, message: ChatMessage): Result<Unit> {
        return try {
            val docRef = chatsCollection.document(chatId)
                .collection("messages").document()
            val msgWithId = message.copy(id = docRef.id)
            docRef.set(msgWithId).await()

            // Update last message metadata
            chatsCollection.document(chatId).set(
                mapOf(
                    "lastMessage" to message.text,
                    "lastTimestamp" to message.timestamp,
                    "lastSenderId" to message.senderId,
                    "participants" to chatId.split("_")
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to messages in real-time using Firestore snapshots
     */
    fun listenToMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener: ListenerRegistration = chatsCollection.document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get list of active chats for a user
     */
    suspend fun getUserChats(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = chatsCollection
                .whereArrayContains("participants", userId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.map { doc ->
                doc.data?.plus("chatId" to doc.id) ?: emptyMap()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Mark messages as read
     */
    suspend fun markAsRead(chatId: String, messageIds: List<String>) {
        try {
            val batch = firestore.batch()
            messageIds.forEach { msgId ->
                val ref = chatsCollection.document(chatId)
                    .collection("messages").document(msgId)
                batch.update(ref, "isRead", true)
            }
            batch.commit().await()
        } catch (_: Exception) {}
    }
}
