package com.meditech.hemav.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.meditech.hemav.data.model.ForumPost
import com.meditech.hemav.data.model.ForumReply
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ForumRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("forum_posts")
    private val repliesCollection = firestore.collection("forum_replies")

    fun getAllPosts(): Flow<List<ForumPost>> = callbackFlow {
        val listener = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(ForumPost::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    fun getPostReplies(postId: String): Flow<List<ForumReply>> = callbackFlow {
        val listener = repliesCollection
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val replies = snapshot?.toObjects(ForumReply::class.java) ?: emptyList()
                trySend(replies)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: ForumPost): Result<Unit> {
        return try {
            val docRef = postsCollection.document()
            val finalPost = post.copy(id = docRef.id)
            docRef.set(finalPost).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReply(reply: ForumReply): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            // 1. Add reply document
            val replyRef = repliesCollection.document()
            val finalReply = reply.copy(id = replyRef.id)
            batch.set(replyRef, finalReply)
            
            // 2. Increment reply count on post
            val postRef = postsCollection.document(reply.postId)
            batch.update(postRef, "replyCount", FieldValue.increment(1))
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upvotePost(postId: String): Result<Unit> {
        return try {
            postsCollection.document(postId)
                .update("upvotes", FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
