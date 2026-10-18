package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.SparkDatabase
import com.example.data.local.SparkPreferencesRepository
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.model.*
import com.example.data.remote.GeminiService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SparkRepository(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = SparkDatabase.getInstance(context)
    private val conversationDao = database.conversationDao()
    private val chatMessageDao = database.chatMessageDao()
    private val projectDao = database.projectDao()

    val preferences = SparkPreferencesRepository(context)
    private val geminiService = GeminiService()

    private val db: FirebaseFirestore by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w("SparkRepository", "Initializing default firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    private val auth = Firebase.auth

    // Local StateFlows backed by Room DB
    private val _localConversations = MutableStateFlow<List<Conversation>>(sampleConversations())
    val localConversations: StateFlow<List<Conversation>> = _localConversations.asStateFlow()

    private val _localMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(sampleMessages())
    val localMessages: StateFlow<Map<String, List<ChatMessage>>> = _localMessages.asStateFlow()

    private val _projects = MutableStateFlow<List<ProjectItem>>(sampleProjects())
    val projects: StateFlow<List<ProjectItem>> = _projects.asStateFlow()

    private val _memoryItems = MutableStateFlow<List<MemoryItem>>(sampleMemory())
    val memoryItems: StateFlow<List<MemoryItem>> = _memoryItems.asStateFlow()

    private val _createdArtAssets = MutableStateFlow<List<GeneratedArtAsset>>(sampleAssets())
    val createdArtAssets: StateFlow<List<GeneratedArtAsset>> = _createdArtAssets.asStateFlow()

    init {
        // Initialize Room DB with initial seeds if empty, and collect reactive flows
        scope.launch {
            if (conversationDao.count() == 0) {
                val seedEntities = sampleConversations().map { ConversationEntity.fromModel(it) }
                conversationDao.insertAll(seedEntities)
            }

            if (chatMessageDao.count() == 0) {
                val seedMsgs = sampleMessages().values.flatten().map { ChatMessageEntity.fromModel(it) }
                chatMessageDao.insertAll(seedMsgs)
            }

            if (projectDao.count() == 0) {
                val seedProjects = sampleProjects().map { ProjectEntity.fromModel(it) }
                projectDao.insertAll(seedProjects)
            }

            // Collect conversations from Room DB
            launch {
                conversationDao.getAllConversations().collect { entities ->
                    if (entities.isNotEmpty()) {
                        _localConversations.value = entities.map { it.toModel() }
                    }
                }
            }

            // Collect projects from Room DB
            launch {
                projectDao.getAllProjects().collect { entities ->
                    if (entities.isNotEmpty()) {
                        _projects.value = entities.map { it.toModel() }
                    }
                }
            }
        }
    }

    fun getStreamForPrompt(
        model: String,
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false,
        enableThinkingHigh: Boolean = false
    ): Flow<String> {
        return geminiService.streamGenerateContent(
            model = model,
            prompt = prompt,
            history = history,
            enableSearchGrounding = enableSearchGrounding,
            enableMapsGrounding = enableMapsGrounding,
            enableThinkingHigh = enableThinkingHigh
        )
    }

    suspend fun analyzeImageWithPrompt(model: String, prompt: String, imageBase64: String?): String {
        return geminiService.generateContent(model, prompt, imageBase64)
    }

    suspend fun pingService(): Pair<Boolean, Long> {
        return geminiService.pingGemini()
    }

    fun searchWithGrounding(query: String) = geminiService.searchWithGrounding(query)

    fun runDeepResearch(topic: String) = geminiService.runDeepResearch(topic)

    suspend fun generateQuiz(subject: String): StudyQuizCard = geminiService.generateDynamicQuiz(subject)

    fun reviewOrTransformCode(code: String, language: String, action: String) =
        geminiService.reviewOrTransformCode(code, language, action)

    fun refineCanvasDocument(content: String, instruction: String) =
        geminiService.refineCanvasDocument(content, instruction)

    suspend fun generateImage(prompt: String, aspectRatio: String, size: String): Result<String> {
        return geminiService.generateImage(prompt, aspectRatio, size)
    }

    suspend fun createNewConversation(title: String, model: String): String {
        val convId = UUID.randomUUID().toString()
        val conv = Conversation(
            id = convId,
            userId = auth.currentUser?.uid ?: "guest",
            title = title,
            model = model,
            snippet = "New conversation started",
            isPinned = false,
            isArchived = false
        )
        _localConversations.value = listOf(conv) + _localConversations.value.filter { it.id != convId }

        scope.launch {
            conversationDao.insertConversation(ConversationEntity.fromModel(conv))
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                db.collection("users").document(uid)
                    .collection("conversations").document(convId)
                    .set(conv).await()
            } catch (e: Exception) {
                Log.w("SparkRepository", "Firestore write failed, cached locally", e)
            }
        }
        return convId
    }

    suspend fun renameConversation(id: String, newTitle: String) {
        _localConversations.value = _localConversations.value.map {
            if (it.id == id) it.copy(title = newTitle) else it
        }
        scope.launch {
            conversationDao.renameConversation(id, newTitle)
        }
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                db.collection("users").document(uid)
                    .collection("conversations").document(id)
                    .update("title", newTitle).await()
            } catch (e: Exception) {
                Log.w("SparkRepository", "Firestore rename failed", e)
            }
        }
    }

    suspend fun togglePinConversation(id: String, isPinned: Boolean) {
        _localConversations.value = _localConversations.value.map {
            if (it.id == id) it.copy(isPinned = isPinned) else it
        }.sortedWith(compareByDescending<Conversation> { it.isPinned })

        scope.launch {
            conversationDao.setPinned(id, isPinned)
        }
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                db.collection("users").document(uid)
                    .collection("conversations").document(id)
                    .update("isPinned", isPinned).await()
            } catch (e: Exception) {
                Log.w("SparkRepository", "Firestore pin update failed", e)
            }
        }
    }

    suspend fun deleteConversation(id: String) {
        _localConversations.value = _localConversations.value.filter { it.id != id }
        val updatedMap = _localMessages.value.toMutableMap()
        updatedMap.remove(id)
        _localMessages.value = updatedMap

        scope.launch {
            conversationDao.deleteConversation(id)
            chatMessageDao.deleteMessagesForConversation(id)
        }
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                db.collection("users").document(uid)
                    .collection("conversations").document(id)
                    .delete().await()
            } catch (e: Exception) {
                Log.w("SparkRepository", "Firestore delete failed", e)
            }
        }
    }

    fun searchConversations(query: String): Flow<List<Conversation>> {
        if (query.isBlank()) {
            return localConversations
        }
        return conversationDao.searchConversations(query).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun saveMessage(message: ChatMessage) {
        val currentList = _localMessages.value[message.conversationId] ?: emptyList()
        val updatedList = currentList.filter { it.id != message.id } + message
        _localMessages.value = _localMessages.value + (message.conversationId to updatedList)

        // Update snippet in conversation if non-empty
        if (message.content.isNotBlank()) {
            val snippetText = message.content.take(80).replace("\n", " ")
            _localConversations.value = _localConversations.value.map {
                if (it.id == message.conversationId) it.copy(snippet = snippetText) else it
            }
        }

        scope.launch {
            chatMessageDao.insertMessage(ChatMessageEntity.fromModel(message))
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                db.collection("users").document(uid)
                    .collection("conversations").document(message.conversationId)
                    .collection("messages").document(message.id.ifEmpty { UUID.randomUUID().toString() })
                    .set(message).await()
            } catch (e: Exception) {
                Log.w("SparkRepository", "Firestore message write failed", e)
            }
        }
    }

    fun addGeneratedAsset(asset: GeneratedArtAsset) {
        _createdArtAssets.value = listOf(asset) + _createdArtAssets.value
    }

    fun addMemoryItem(key: String, value: String, category: String) {
        val item = MemoryItem(
            id = UUID.randomUUID().toString(),
            userId = auth.currentUser?.uid ?: "guest",
            key = key,
            value = value,
            category = category
        )
        _memoryItems.value = _memoryItems.value + item
    }

    fun removeMemoryItem(id: String) {
        _memoryItems.value = _memoryItems.value.filter { it.id != id }
    }

    fun addProject(name: String, description: String) {
        val project = ProjectItem(
            id = UUID.randomUUID().toString(),
            userId = auth.currentUser?.uid ?: "guest",
            name = name,
            description = description,
            conversationsCount = 1,
            documentsCount = 0
        )
        _projects.value = listOf(project) + _projects.value
        scope.launch {
            projectDao.insertProject(ProjectEntity.fromModel(project))
        }
    }

    fun deleteProject(id: String) {
        _projects.value = _projects.value.filter { it.id != id }
        scope.launch {
            projectDao.deleteProject(id)
        }
    }

    // Sample initial data
    private fun sampleConversations(): List<Conversation> = listOf(
        Conversation("c1", "guest", "Quantum Algorithms & Shor's Method", AIModelType.GEMINI_4_ARGON.id, "Polynomial-time factorization principles explained", isPinned = true),
        Conversation("c2", "guest", "Kotlin Coroutine Flow Buffers", AIModelType.GEMINI_3_8_FLASH.id, "Backpressure mitigation with Channel.CONFLATED", isPinned = true),
        Conversation("c3", "guest", "Solid-State Battery Density 2026", AIModelType.GEMINI_3_8_FLASH.id, "Deep research synthesis across 14 scientific papers", isPinned = false),
        Conversation("c4", "guest", "Cyberpunk Neon Botanical Artwork", AIModelType.GEMINI_3_8_FLASH.id, "Veo 3.1 4K cinematic animation generation", isPinned = false)
    )

    private fun sampleMessages(): Map<String, List<ChatMessage>> = mapOf(
        "c1" to listOf(
            ChatMessage("m1", "c1", "guest", "user", "Explain Shor's algorithm simply, focusing on period finding."),
            ChatMessage(
                "m2", "c1", "guest", "model",
                "Shor's algorithm achieves an exponential speedup over classical factoring by converting prime factorization into finding the period of a modular exponential function f(x) = a^x mod N.\n\n### Core Quantum Mechanics:\n1. **Quantum Superposition**: Evaluates all powers simultaneously.\n2. **Quantum Fourier Transform (QFT)**: Constructively interferes to reveal the hidden period r.\n3. **Classical Post-Processing**: Computes gcd(a^(r/2) ± 1, N) to extract the non-trivial prime factors.",
                model = AIModelType.GEMINI_4_ARGON.id,
                thinkingProcess = "Analyzing computational complexity: Classical number field sieve is sub-exponential O(exp(c n^(1/3))), whereas Shor's algorithm runs in O((log N)^3) on a fault-tolerant quantum computer."
            )
        )
    )

    private fun sampleProjects(): List<ProjectItem> = listOf(
        ProjectItem("p1", "guest", "Android OS Kernel Optimization", "Custom scheduler tuning for Snapdragon 8 Elite", 4, 3),
        ProjectItem("p2", "guest", "Master Thesis: Autonomous Robotics", "Visual SLAM and point cloud localization", 8, 5),
        ProjectItem("p3", "guest", "Fintech Quant Strategy", "Low-latency statistical arbitrage models", 2, 1)
    )

    private fun sampleMemory(): List<MemoryItem> = listOf(
        MemoryItem("mem1", "guest", "Preferred Language", "Kotlin 2.2 with Jetpack Compose", "Developer"),
        MemoryItem("mem2", "guest", "Tone & Style", "Rigorous, technical, highly concise, no filler", "Communication"),
        MemoryItem("mem3", "guest", "Target Architecture", "Clean MVVM with Unidirectional Data Flow", "Architecture")
    )

    private fun sampleAssets(): List<GeneratedArtAsset> = listOf(
        GeneratedArtAsset("a1", "Holographic obsidian crystal in a violet nebula", "IMAGE", "gemini-3-pro-image-preview", "", "1:1", "4K"),
        GeneratedArtAsset("a2", "Cyberpunk botanical drone in Tokyo rain", "VIDEO", "veo-3.1-fast-generate-preview", "", "16:9", "1080p", 5),
        GeneratedArtAsset("a3", "Ambient celestial synthwave chill track", "MUSIC", "lyria-3-clip-preview", "", "Audio", "320kbps", 30)
    )
}
