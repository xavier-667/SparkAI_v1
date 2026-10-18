package com.example

import com.example.data.model.*
import com.example.ui.navigation.SparkScreen
import org.junit.Assert.*
import org.junit.Test

class SparkRepositoryTest {

    @Test
    fun verifyModelTypesConfiguration() {
        val argon = AIModelType.GEMINI_4_ARGON
        assertEquals("gemini-4-argon", argon.id)
        assertTrue(argon.isThinkingCapable)

        val flash = AIModelType.GEMINI_3_8_FLASH
        assertEquals("gemini-3.8-flash", flash.id)
        assertFalse(flash.isThinkingCapable)

        val lite = AIModelType.GEMINI_3_1_FLASH_LITE
        assertEquals("gemini-3.1-flash-lite", lite.id)

        val live = AIModelType.GEMINI_3_8_LIVE
        assertEquals("gemini-3.8-live", live.id)
    }

    @Test
    fun verifySupportedAspectRatios() {
        val values = SupportedAspectRatios.map { it.value }
        assertTrue(values.contains("1:1"))
        assertTrue(values.contains("16:9"))
        assertTrue(values.contains("9:16"))
        assertTrue(values.contains("4:3"))
        assertTrue(values.contains("21:9"))
    }

    @Test
    fun verifySupportedImageSizes() {
        assertEquals(listOf("1K", "2K", "4K"), SupportedImageSizes)
    }

    @Test
    fun verifyChatMessageCreation() {
        val message = ChatMessage(
            id = "msg_123",
            conversationId = "conv_456",
            role = "user",
            content = "Explain quantum computing"
        )
        assertEquals("msg_123", message.id)
        assertEquals("user", message.role)
        assertEquals("Explain quantum computing", message.content)
        assertFalse(message.isStreaming)
    }

    @Test
    fun verifyConversationAttributesAndPinning() {
        val conv = Conversation(
            id = "conv_test_1",
            userId = "user_abc",
            title = "Architecture Review",
            model = AIModelType.GEMINI_3_8_FLASH.id,
            snippet = "Preview snippet text",
            isPinned = true,
            isArchived = false
        )
        assertEquals("conv_test_1", conv.id)
        assertTrue(conv.isPinned)
        assertFalse(conv.isArchived)
        assertEquals(AIModelType.GEMINI_3_8_FLASH.id, conv.model)
    }

    @Test
    fun verifyAllNavigationDestinationsAreUniqueAndValid() {
        val screens = SparkScreen.values()
        assertEquals(19, screens.size)

        val routes = screens.map { it.route }
        assertEquals(routes.size, routes.distinct().size)

        screens.forEach { screen ->
            assertTrue(screen.route.isNotBlank())
            assertTrue(screen.title.isNotBlank())
            assertTrue(screen.category.isNotBlank())
            assertTrue(screen.description.isNotBlank())
        }
    }

    @Test
    fun verifyGroundingCitationStructure() {
        val citation = GroundingCitation(
            index = 1,
            title = "Official Research Publication",
            url = "https://example.com/research",
            snippet = "Empirical verification achieved."
        )
        assertEquals(1, citation.index)
        assertTrue(citation.url.startsWith("https://"))
        assertTrue(citation.snippet.isNotBlank())
    }

    @Test
    fun verifyStudyQuizCardValidation() {
        val quiz = StudyQuizCard(
            id = "quiz_1",
            subject = "Computer Science",
            question = "What is the time complexity of binary search on a sorted array?",
            options = listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
            correctIndex = 1,
            explanation = "Binary search halves the search interval in each step, yielding logarithmic O(log n) complexity."
        )
        assertEquals(4, quiz.options.size)
        assertTrue(quiz.correctIndex in 0 until quiz.options.size)
        assertEquals("O(log n)", quiz.options[quiz.correctIndex])
        assertTrue(quiz.explanation.isNotBlank())
    }

    @Test
    fun verifyProjectItemAndMemoryItemCreation() {
        val project = ProjectItem(
            id = "p_1",
            userId = "u_1",
            name = "Android Performance Suite",
            description = "Profiling Compose recomposition and frame metrics",
            conversationsCount = 3,
            documentsCount = 2
        )
        assertEquals("Android Performance Suite", project.name)
        assertEquals(3, project.conversationsCount)
        assertEquals(2, project.documentsCount)

        val memory = MemoryItem(
            id = "m_1",
            userId = "u_1",
            key = "Coding Style",
            value = "Strict Material 3 with Kotlin 2.2",
            category = "Architecture"
        )
        assertEquals("Coding Style", memory.key)
        assertEquals("Architecture", memory.category)
    }

    @Test
    fun verifyGeneratedArtAssetModel() {
        val asset = GeneratedArtAsset(
            id = "asset_1",
            prompt = "Cyberpunk cityscape with volumetric neon lighting",
            type = "IMAGE",
            model = "gemini-2.5-flash-image",
            mediaUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
            aspectRatio = "16:9",
            resolution = "2K"
        )
        assertEquals("IMAGE", asset.type)
        assertEquals("gemini-2.5-flash-image", asset.model)
        assertTrue(asset.mediaUrl.startsWith("data:image/jpeg;base64,"))
        assertEquals("16:9", asset.aspectRatio)
    }
}
