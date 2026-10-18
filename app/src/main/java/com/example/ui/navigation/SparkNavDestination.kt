package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class SparkScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val category: String,
    val description: String
) {
    HOME("home", "Home Hub", Icons.Default.Home, "Core", "Greeting, prompt composer & quick access"),
    CHAT("chat", "Chat Assistant", Icons.AutoMirrored.Filled.Chat, "Core", "Multi-turn streaming AI conversation"),
    MODEL_SELECTION("model_selection", "AI Engines", Icons.Default.Tune, "Intelligence", "Gemini 4 Argon, 3.8 Flash & Lite"),
    SEARCH_GROUNDING("search_grounding", "Web Search", Icons.Default.Language, "Intelligence", "Grounded facts with source citations"),
    DEEP_RESEARCH("deep_research", "Deep Research", Icons.Default.Psychology, "Intelligence", "Autonomous web exploration reports"),
    IMAGE_STUDIO("image_studio", "Creative Studios", Icons.Default.Palette, "Creation", "Image, Veo Video & Lyria Music"),
    LIVE_VOICE("live_voice", "Live Voice Call", Icons.Default.Mic, "Real-time", "Low-latency bidirectional speech"),
    CAMERA_VISION("camera_vision", "Camera Inspector", Icons.Default.CameraAlt, "Real-time", "Real-time live visual understanding"),
    DOCUMENT_ANALYSIS("document_analysis", "Document Lab", Icons.Default.Description, "Productivity", "PDF & spreadsheet analysis"),
    CODE_ASSISTANT("code_assistant", "Code Studio", Icons.Default.Code, "Productivity", "Syntax highlight & algorithmic logic"),
    CANVAS_WORKSPACE("canvas_workspace", "Canvas Workspace", Icons.Default.EditNote, "Productivity", "Interactive text & plan editor"),
    STUDY_HUB("study_hub", "Study & Quizzes", Icons.Default.School, "Learning", "Interactive quizzes & conceptual tutor"),
    PROJECTS_MEMORY("projects_memory", "Projects & Memory", Icons.Default.FolderSpecial, "Context", "Persistent memory & file buckets"),
    TOOLS_INTEGRATION("tools_integration", "Tools Hub", Icons.Default.Extension, "Context", "Google Search, Maps & code sandbox"),
    UNIVERSAL_SEARCH("universal_search", "Universal Search", Icons.Default.Search, "Context", "Search across chats, code & docs"),
    SETTINGS("settings", "Settings", Icons.Default.Settings, "System", "Theme, appearance, privacy & auth"),
    DIAGNOSTICS("diagnostics", "Diagnostics", Icons.Default.NetworkCheck, "System", "API latency & Firebase health monitor"),
    STATES_SHOWCASE("states_showcase", "State Showcase", Icons.Default.Layers, "System", "Offline, rate-limits & empty states"),
    ONBOARDING("onboarding", "Welcome", Icons.Default.AutoAwesome, "Account", "Google Sign-In & feature tour")
}
