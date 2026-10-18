package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val userId: String,
    val role: String,
    val content: String,
    val model: String,
    val thinkingProcess: String? = null,
    val imageUrl: String? = null,
    val isStreaming: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    fun toModel(): ChatMessage = ChatMessage(
        id = id,
        conversationId = conversationId,
        userId = userId,
        role = role,
        content = content,
        model = model,
        thinkingProcess = thinkingProcess,
        imageUrl = imageUrl,
        isStreaming = isStreaming
    )

    companion object {
        fun fromModel(model: ChatMessage): ChatMessageEntity = ChatMessageEntity(
            id = model.id,
            conversationId = model.conversationId,
            userId = model.userId,
            role = model.role,
            content = model.content,
            model = model.model,
            thinkingProcess = model.thinkingProcess,
            imageUrl = model.imageUrl,
            isStreaming = model.isStreaming
        )
    }
}
