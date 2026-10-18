package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ProjectEntity

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        ProjectEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SparkDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: SparkDatabase? = null

        fun getInstance(context: Context): SparkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SparkDatabase::class.java,
                    "spark_ai.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
