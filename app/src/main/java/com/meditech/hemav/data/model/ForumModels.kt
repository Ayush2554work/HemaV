package com.meditech.hemav.data.model

data class ForumPost(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "", // "patient", "doctor"
    val isDoctorVerified: Boolean = false,
    val authorProfilePicUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 0,
    val tags: List<String> = emptyList(),
    val replyCount: Int = 0
)

data class ForumReply(
    val id: String = "",
    val postId: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "", // "patient", "doctor"
    val isDoctorVerified: Boolean = false,
    val authorProfilePicUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
