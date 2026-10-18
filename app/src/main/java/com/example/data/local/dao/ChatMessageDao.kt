package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :convId ORDER BY timestampMillis ASC")
    fun getMessagesForConversation(convId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE conversationId = :convId")
    suspend fun deleteMessagesForConversation(convId: String)

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun count(): Int
}
