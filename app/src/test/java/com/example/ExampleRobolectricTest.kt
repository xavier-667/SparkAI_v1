package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SparkDatabase
import com.example.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var inMemoryDb: SparkDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, SparkDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    @Test
    fun readStringFromContext() {
        val appName = context.getString(R.string.app_name)
        assertEquals("SparkAI", appName)
    }

    @Test
    fun verifyRoomDatabaseConversationPersistence() = runBlocking {
        val conversationDao = inMemoryDb.conversationDao()
        assertEquals(0, conversationDao.count())

        val entity = ConversationEntity(
            id = "conv_test_100",
            userId = "user_test",
            title = "Robolectric Verification Chat",
            model = "gemini-3.8-flash",
            snippet = "Testing in-memory Room database",
            isPinned = true,
            isArchived = false,
            createdAtMillis = System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis()
        )
        conversationDao.insertConversation(entity)

        assertEquals(1, conversationDao.count())

        val retrieved = conversationDao.getConversationById("conv_test_100").first()
        assertNotNull(retrieved)
        assertEquals("Robolectric Verification Chat", retrieved?.title)
        assertTrue(retrieved?.isPinned == true)

        conversationDao.renameConversation("conv_test_100", "Renamed Title")
        val updated = conversationDao.getConversationById("conv_test_100").first()
        assertEquals("Renamed Title", updated?.title)

        conversationDao.deleteConversation("conv_test_100")
        assertEquals(0, conversationDao.count())
    }
}
