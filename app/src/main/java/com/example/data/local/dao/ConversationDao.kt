package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversationById(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR snippet LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET title = :newTitle, updatedAtMillis = :updatedAt WHERE id = :id")
    suspend fun renameConversation(id: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: String, isPinned: Boolean)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun count(): Int
}
