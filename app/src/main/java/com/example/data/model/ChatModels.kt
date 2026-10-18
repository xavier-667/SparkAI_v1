package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AIModelType(
    val id: String,
    val displayName: String,
    val description: String,
    val badge: String,
    val isThinkingCapable: Boolean
) {
    GEMINI_4_ARGON(
        id = "gemini-4-argon",
        displayName = "Gemini 4 Argon",
        description = "Supreme reasoning, code synthesis & deep thinking",
        badge = "Thinking: High",
        isThinkingCapable = true
    ),
    GEMINI_3_8_FLASH(
        id = "gemini-3.8-flash",
        displayName = "Gemini 3.8 Flash",
        description = "High-speed multimodal intelligence with Search & Maps grounding",
        badge = "Multimodal",
        isThinkingCapable = false
    ),
    GEMINI_3_1_FLASH_LITE(
        id = "gemini-3.1-flash-lite",
        displayName = "Gemini 3.1 Flash-Lite",
        description = "Ultra-low latency (<200ms) for instant Q&A",
        badge = "Sub-200ms",
        isThinkingCapable = false
    ),
    GEMINI_3_8_LIVE(
        id = "gemini-3.8-live",
        displayName = "Gemini 3.8 Live",
        description = "Real-time bidirectional speech & camera stream",
        badge = "Live Audio",
        isThinkingCapable = false
    )
}

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val model: String = AIModelType.GEMINI_3_8_FLASH.id,
    val snippet: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val userId: String = "",
    val role: String = "user", // "user" or "model"
    val content: String = "",
    val model: String = AIModelType.GEMINI_3_8_FLASH.id,
    val thinkingProcess: String? = null,
    val citations: List<String> = emptyList(),
    val imageUrl: String? = null,
    val isStreaming: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class GroundingCitation(
    val index: Int,
    val title: String,
    val url: String,
    val snippet: String
)
