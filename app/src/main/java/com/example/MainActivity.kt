package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AIModelType
import com.example.data.model.ChatMessage
import com.example.data.repository.SparkRepository
import com.example.ui.components.SparkComposer
import com.example.ui.components.SparkNavDrawerSheet
import com.example.ui.components.SparkTopBar
import com.example.ui.navigation.SparkScreen
import com.example.ui.screens.*
import com.example.ui.theme.SparkAITheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tts = TextToSpeech(this, this)

        setContent {
            var darkTheme by remember { mutableStateOf(true) }

            SparkAITheme(darkTheme = darkTheme) {
                SparkAIApp(
                    darkTheme = darkTheme,
                    onToggleDarkTheme = { darkTheme = it },
                    onSpeak = { text -> speakText(text) }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true
        }
    }

    private fun speakText(text: String) {
        if (isTtsReady) {
            // Strip markdown backticks or hashes for clean audio reading
            val cleanText = text.replace("```[a-zA-Z]*".toRegex(), "")
                .replace("```", "")
                .replace("#+", "")
                .replace("\\*\\*".toRegex(), "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "SparkTTS")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun SparkAIApp(
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SparkRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // Navigation & Screen State
    var currentScreen by remember { mutableStateOf(SparkScreen.HOME) }
    var showOnboarding by remember { mutableStateOf(false) }
    var activeConversationId by remember { mutableStateOf("c1") }

    // Model & Hyperparameters
    var selectedModel by remember { mutableStateOf(AIModelType.GEMINI_3_8_FLASH) }
    var showModelSelector by remember { mutableStateOf(false) }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var thinkingHigh by remember { mutableStateOf(true) }

    // Composer & Streaming State
    var composerText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    var enableSearchGrounding by remember { mutableStateOf(false) }
    var enableThinkingChip by remember { mutableStateOf(true) }
    var streamingJob by remember { mutableStateOf<Job?>(null) }

    // Data States from Repository
    val conversations by repository.localConversations.collectAsStateWithLifecycle()
    val allMessages by repository.localMessages.collectAsStateWithLifecycle()
    val activeMessages = allMessages[activeConversationId] ?: emptyList()
    val projects by repository.projects.collectAsStateWithLifecycle()
    val memoryItems by repository.memoryItems.collectAsStateWithLifecycle()
    val artAssets by repository.createdArtAssets.collectAsStateWithLifecycle()

    // Firebase Auth observation
    var currentUserEmail by remember { mutableStateOf(Firebase.auth.currentUser?.email) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // BackHandler: return to HOME if on sub-screen or close drawer if open
    BackHandler(enabled = drawerState.isOpen || currentScreen != SparkScreen.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentScreen = SparkScreen.HOME
        }
    }

    // Function to send prompt and stream response
    fun handleSendPrompt(prompt: String) {
        if (prompt.isBlank() || isStreaming) return

        val userMsgId = UUID.randomUUID().toString()
        val userMsg = ChatMessage(
            id = userMsgId,
            conversationId = activeConversationId,
            role = "user",
            content = prompt,
            model = selectedModel.id
        )

        val modelMsgId = UUID.randomUUID().toString()
        val initialModelMsg = ChatMessage(
            id = modelMsgId,
            conversationId = activeConversationId,
            role = "model",
            content = "",
            model = selectedModel.id,
            isStreaming = true,
            thinkingProcess = if (selectedModel == AIModelType.GEMINI_4_ARGON) "Synthesizing deep multidimensional logic..." else null
        )

        coroutineScope.launch {
            repository.saveMessage(userMsg)
            repository.saveMessage(initialModelMsg)
            composerText = ""
            currentScreen = SparkScreen.CHAT
            isStreaming = true

            val fullAccumulated = StringBuilder()
            val streamFlow = repository.getStreamForPrompt(
                model = selectedModel.id,
                prompt = prompt,
                history = activeMessages.takeLast(6).map { it.role to it.content },
                enableSearchGrounding = enableSearchGrounding,
                enableThinkingHigh = thinkingHigh || selectedModel == AIModelType.GEMINI_4_ARGON
            )

            streamingJob = launch {
                streamFlow.collectLatest { chunk ->
                    fullAccumulated.append(chunk)
                    repository.saveMessage(
                        initialModelMsg.copy(
                            content = fullAccumulated.toString(),
                            isStreaming = true
                        )
                    )
                }
                isStreaming = false
                repository.saveMessage(
                    initialModelMsg.copy(
                        content = fullAccumulated.toString(),
                        isStreaming = false
                    )
                )
            }
        }
    }

    if (showOnboarding) {
        OnboardingScreen(
            onContinue = {
                showOnboarding = false
                currentUserEmail = Firebase.auth.currentUser?.email
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SparkNavDrawerSheet(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        currentScreen = screen
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        coroutineScope.launch {
                            val newId = repository.createNewConversation("New Conversation", selectedModel.id)
                            activeConversationId = newId
                            currentScreen = SparkScreen.CHAT
                            drawerState.close()
                        }
                    },
                    userEmail = currentUserEmail,
                    onSignInClick = {
                        showOnboarding = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSignOutClick = {
                        Firebase.auth.signOut()
                        currentUserEmail = null
                        Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    SparkTopBar(
                        title = currentScreen.title,
                        modelName = selectedModel.displayName,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onModelClick = { showModelSelector = true }
                    )
                },
                bottomBar = {
                    // Show prompt composer on input-centric sub-views (HomeScreen has its own bottom prompt bar)
                    val showComposer = currentScreen in listOf(
                        SparkScreen.CHAT,
                        SparkScreen.SEARCH_GROUNDING,
                        SparkScreen.DOCUMENT_ANALYSIS,
                        SparkScreen.CODE_ASSISTANT
                    )
                    if (showComposer) {
                        SparkComposer(
                            text = composerText,
                            onTextChange = { composerText = it },
                            onSend = { handleSendPrompt(it) },
                            onCameraClick = { currentScreen = SparkScreen.CAMERA_VISION },
                            onVoiceClick = { currentScreen = SparkScreen.LIVE_VOICE },
                            onAttachClick = { currentScreen = SparkScreen.DOCUMENT_ANALYSIS },
                            isStreaming = isStreaming,
                            onStopStreaming = {
                                streamingJob?.cancel()
                                isStreaming = false
                            },
                            enableSearchGrounding = enableSearchGrounding,
                            onToggleSearchGrounding = { enableSearchGrounding = !enableSearchGrounding },
                            enableThinking = enableThinkingChip,
                            onToggleThinking = { enableThinkingChip = !enableThinkingChip }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        SparkScreen.HOME -> HomeScreen(
                            conversations = conversations,
                            onSelectConversation = { convId ->
                                activeConversationId = convId
                                currentScreen = SparkScreen.CHAT
                            },
                            onNavigate = { currentScreen = it },
                            onSendPrompt = { prompt -> handleSendPrompt(prompt) },
                            onQuickPrompt = { prompt -> handleSendPrompt(prompt) },
                            userDisplayName = currentUserEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                        )

                        SparkScreen.CHAT -> ChatScreen(
                            messages = activeMessages,
                            activeModelName = selectedModel.displayName,
                            onSpeak = onSpeak,
                            onRetry = {
                                val lastUser = activeMessages.lastOrNull { it.role == "user" }
                                if (lastUser != null) {
                                    handleSendPrompt(lastUser.content)
                                }
                            }
                        )

                        SparkScreen.MODEL_SELECTION -> {
                            // Automatically pops up bottom sheet
                            HomeScreen(
                                conversations = conversations,
                                onSelectConversation = {
                                    activeConversationId = it
                                    currentScreen = SparkScreen.CHAT
                                },
                                onNavigate = { currentScreen = it },
                                onSendPrompt = { handleSendPrompt(it) },
                                onQuickPrompt = { handleSendPrompt(it) },
                                userDisplayName = currentUserEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                            )
                            LaunchedEffect(Unit) { showModelSelector = true }
                        }

                        SparkScreen.SEARCH_GROUNDING -> SearchGroundedScreen(
                            onSearchQuery = { query -> handleSendPrompt(query) },
                            onGroundingSearch = { query -> repository.searchWithGrounding(query) }
                        )

                        SparkScreen.DEEP_RESEARCH -> DeepResearchScreen(
                            onRunDeepResearch = { topic -> repository.runDeepResearch(topic) }
                        )

                        SparkScreen.IMAGE_STUDIO -> ImageStudioScreen(
                            assets = artAssets,
                            onGenerateImage = { prompt, ratio, size ->
                                coroutineScope.launch {
                                    Toast.makeText(context, "Generating artwork with Gemini...", Toast.LENGTH_SHORT).show()
                                    val result = repository.generateImage(prompt, ratio, size)
                                    result.onSuccess { dataUri ->
                                        val asset = com.example.data.model.GeneratedArtAsset(
                                            id = UUID.randomUUID().toString(),
                                            prompt = prompt,
                                            type = "IMAGE",
                                            model = "gemini-2.5-flash-image",
                                            mediaUrl = dataUri,
                                            aspectRatio = ratio,
                                            resolution = size
                                        )
                                        repository.addGeneratedAsset(asset)
                                        Toast.makeText(context, "Artwork generated!", Toast.LENGTH_SHORT).show()
                                    }.onFailure { error ->
                                        Toast.makeText(context, error.message ?: "Image generation failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onGenerateVeoVideo = { prompt, isLandscape ->
                                val asset = com.example.data.model.GeneratedArtAsset(
                                    id = UUID.randomUUID().toString(),
                                    prompt = prompt,
                                    type = "VIDEO",
                                    model = "veo-3.1-fast-generate-preview",
                                    mediaUrl = "",
                                    aspectRatio = if (isLandscape) "16:9" else "9:16",
                                    resolution = "1080p",
                                    durationSeconds = 6
                                )
                                repository.addGeneratedAsset(asset)
                                Toast.makeText(context, "Veo 3.1 video queued (Preview)", Toast.LENGTH_SHORT).show()
                            },
                            onGenerateLyriaMusic = { prompt, isShortClip ->
                                val asset = com.example.data.model.GeneratedArtAsset(
                                    id = UUID.randomUUID().toString(),
                                    prompt = prompt,
                                    type = "MUSIC",
                                    model = if (isShortClip) "lyria-3-clip-preview" else "lyria-3-pro-preview",
                                    mediaUrl = "",
                                    aspectRatio = "Audio",
                                    resolution = "320kbps",
                                    durationSeconds = if (isShortClip) 30 else 180
                                )
                                repository.addGeneratedAsset(asset)
                                Toast.makeText(context, "Lyria 3 track queued (Preview)", Toast.LENGTH_SHORT).show()
                            }
                        )

                        SparkScreen.LIVE_VOICE -> LiveVoiceScreen(
                            onEndCall = { currentScreen = SparkScreen.HOME },
                            onGenerateVoiceReply = { voicePrompt ->
                                repository.getStreamForPrompt(
                                    model = "gemini-3.5-flash",
                                    prompt = voicePrompt
                                )
                            }
                        )

                        SparkScreen.CAMERA_VISION -> CameraVisionScreen(
                            onInspectAction = { prompt -> handleSendPrompt(prompt) },
                            onAnalyzeImage = { prompt, base64 ->
                                repository.analyzeImageWithPrompt(
                                    model = "gemini-3.5-flash",
                                    prompt = prompt,
                                    imageBase64 = base64
                                )
                            }
                        )

                        SparkScreen.DOCUMENT_ANALYSIS -> DocumentScreen(
                            onAnalyzePrompt = { prompt -> handleSendPrompt(prompt) },
                            onAnalyzeDocumentText = { docName, content, query ->
                                repository.getStreamForPrompt(
                                    model = "gemini-3.1-pro-preview",
                                    prompt = "Document: $docName\n\nContent:\n$content\n\nTask: $query"
                                )
                            }
                        )

                        SparkScreen.CODE_ASSISTANT -> CodeAssistantScreen(
                            onOpenInCanvas = { code ->
                                currentScreen = SparkScreen.CANVAS_WORKSPACE
                            },
                            onReviewCode = { code, lang, action ->
                                repository.reviewOrTransformCode(code, lang, action)
                            }
                        )

                        SparkScreen.CANVAS_WORKSPACE -> CanvasWorkspaceScreen(
                            onRefineDocument = { content, instruction ->
                                repository.refineCanvasDocument(content, instruction)
                            }
                        )

                        SparkScreen.STUDY_HUB -> StudyHubScreen(
                            onAskTutor = { prompt -> handleSendPrompt(prompt) },
                            onGenerateDynamicQuiz = { subject -> repository.generateQuiz(subject) }
                        )

                        SparkScreen.PROJECTS_MEMORY -> ProjectsScreen(
                            projects = projects,
                            memoryItems = memoryItems,
                            onAddProject = { name, desc -> repository.addProject(name, desc) },
                            onAddMemory = { k, v, cat -> repository.addMemoryItem(k, v, cat) },
                            onDeleteMemory = { id -> repository.removeMemoryItem(id) }
                        )

                        SparkScreen.TOOLS_INTEGRATION -> ToolsScreen(
                            onPingGateway = { repository.pingService() }
                        )

                        SparkScreen.UNIVERSAL_SEARCH -> UniversalSearchScreen(
                            onSelectResult = { title ->
                                currentScreen = SparkScreen.CHAT
                            }
                        )

                        SparkScreen.SETTINGS -> SettingsScreen(
                            darkTheme = darkTheme,
                            onToggleDarkTheme = onToggleDarkTheme,
                            userEmail = currentUserEmail,
                            onSignIn = { showOnboarding = true },
                            onSignOut = {
                                Firebase.auth.signOut()
                                currentUserEmail = null
                            }
                        )

                        SparkScreen.DIAGNOSTICS -> DiagnosticsScreen(
                            onPingTest = { repository.pingService() }
                        )

                        SparkScreen.STATES_SHOWCASE -> StatesShowcaseScreen()

                        SparkScreen.ONBOARDING -> OnboardingScreen(
                            onContinue = {
                                showOnboarding = false
                                currentScreen = SparkScreen.HOME
                            }
                        )
                    }
                }
            }
        }

        // Model Selection Modal Bottom Sheet
        if (showModelSelector) {
            ModelSelectionSheet(
                selectedModel = selectedModel,
                onModelSelected = {
                    selectedModel = it
                    showModelSelector = false
                },
                temperature = temperature,
                onTemperatureChange = { temperature = it },
                thinkingHigh = thinkingHigh,
                onThinkingHighChange = { thinkingHigh = it },
                onDismiss = { showModelSelector = false }
            )
        }
    }
}
