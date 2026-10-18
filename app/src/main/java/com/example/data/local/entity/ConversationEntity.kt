package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Conversation

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val model: String,
    val snippet: String,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toModel(): Conversation = Conversation(
        id = id,
        userId = userId,
        title = title,
        model = model,
        snippet = snippet,
        isPinned = isPinned,
        isArchived = isArchived
    )

    companion object {
        fun fromModel(model: Conversation): ConversationEntity = ConversationEntity(
            id = model.id,
            userId = model.userId,
            title = model.title,
            model = model.model,
            snippet = model.snippet,
            isPinned = model.isPinned,
            isArchived = model.isArchived
        )
    }
}
