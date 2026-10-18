package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.ui.navigation.SparkScreen
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkRose
import com.example.ui.theme.SparkVioletGlow
import java.util.Calendar

@Composable
fun HomeScreen(
    conversations: List<Conversation>,
    onSelectConversation: (String) -> Unit,
    onNavigate: (SparkScreen) -> Unit,
    onSendPrompt: (String) -> Unit = {},
    onQuickPrompt: (String) -> Unit = onSendPrompt,
    userDisplayName: String? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var promptText by remember { mutableStateOf("") }
    var searchGroundingActive by remember { mutableStateOf(false) }
    var thinkingHighActive by remember { mutableStateOf(true) }

    // Pulsing animation for the AI status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Dynamic time-based greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Welcome to night mode"
        }
    }

    val displayName = userDisplayName ?: "Alex"

    fun submitCurrentPrompt(text: String) {
        if (text.isNotBlank()) {
            onSendPrompt(text)
            onQuickPrompt(text)
            promptText = ""
            focusManager.clearFocus()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Scrollable Home Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 150.dp)
        ) {
            // 1. Hero Greeting Banner with Atmospheric Radial Glow
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_greeting_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        SparkVioletGlow.copy(alpha = 0.24f),
                                        SparkCyan.copy(alpha = 0.10f),
                                        Color.Transparent
                                    ),
                                    radius = 450f
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$greeting, $displayName",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            letterSpacing = (-0.5).sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Spark AI Emblem",
                                        tint = SparkCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SparkCyan.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .scale(pulseScale)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Pro Online",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SparkCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "What would you like to explore or create with SparkAI today?",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. Specialized Workspaces Quick-Launcher
            item {
                Text(
                    text = "FLAGSHIP WORKSPACES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SparkCyan,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        CapabilityCard(
                            title = "Deep Research",
                            subtitle = "Multi-source synthesis",
                            icon = Icons.Default.Psychology,
                            accentColor = SparkCyan,
                            onClick = { onNavigate(SparkScreen.DEEP_RESEARCH) },
                            testTag = "card_deep_research"
                        )
                    }
                    item {
                        CapabilityCard(
                            title = "Creative Studio",
                            subtitle = "Veo 3.1 & Lyria 3",
                            icon = Icons.Default.Palette,
                            accentColor = SparkVioletGlow,
                            onClick = { onNavigate(SparkScreen.IMAGE_STUDIO) },
                            testTag = "card_creative_studio"
                        )
                    }
                    item {
                        CapabilityCard(
                            title = "Live Voice",
                            subtitle = "Bidirectional audio",
                            icon = Icons.Default.Mic,
                            accentColor = SparkRose,
                            onClick = { onNavigate(SparkScreen.LIVE_VOICE) },
                            testTag = "card_live_voice"
                        )
                    }
                    item {
                        CapabilityCard(
                            title = "Code Studio",
                            subtitle = "Isolated sandbox",
                            icon = Icons.Default.Code,
                            accentColor = Color(0xFF10B981),
                            onClick = { onNavigate(SparkScreen.CODE_ASSISTANT) },
                            testTag = "card_code_assistant"
                        )
                    }
                    item {
                        CapabilityCard(
                            title = "Study Hub",
                            subtitle = "Flashcards & quizzes",
                            icon = Icons.Default.School,
                            accentColor = Color(0xFFF59E0B),
                            onClick = { onNavigate(SparkScreen.STUDY_HUB) },
                            testTag = "card_study_hub"
                        )
                    }
                }
            }

            // 3. Suggested Prompt Exploration Chips
            item {
                Text(
                    text = "SUGGESTED EXPLORATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                val suggestions = listOf(
                    "Explain Shor's quantum factoring algorithm simply",
                    "Draft an Android architecture specification with Jetpack Compose",
                    "Analyze solid-state battery energy density breakthroughs in 2026",
                    "Generate a cyberpunk rooftop artwork in 16:9 cinematic ratio"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.forEachIndexed { idx, prompt ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("suggestion_prompt_$idx")
                                .clickable {
                                    promptText = prompt
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SparkCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { submitCurrentPrompt(prompt) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Send suggestion",
                                        tint = SparkCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Recent Conversations
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "RECENT CONVERSATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    )
                    TextButton(
                        onClick = { onNavigate(SparkScreen.CHAT) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("View All", fontSize = 12.sp, color = SparkCyan, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (conversations.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No recent chats yet. Start a new topic below!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        conversations.take(3).forEach { conv ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectConversation(conv.id) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SparkCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = SparkCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = conv.title.ifEmpty { "Conversation" },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = conv.snippet.ifEmpty { "Tap to view conversation" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Core Bottom Prompt Bar Pinned for User Interaction
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .testTag("home_bottom_prompt_bar")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Capability Toggle Chips (Web Search & Deep Think)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    FilterChip(
                        selected = searchGroundingActive,
                        onClick = { searchGroundingActive = !searchGroundingActive },
                        label = { Text("Web Search", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SparkCyan.copy(alpha = 0.2f),
                            selectedLabelColor = SparkCyan,
                            selectedLeadingIconColor = SparkCyan
                        ),
                        modifier = Modifier.testTag("chip_search_grounding")
                    )

                    FilterChip(
                        selected = thinkingHighActive,
                        onClick = { thinkingHighActive = !thinkingHighActive },
                        label = { Text("Deep Think", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SparkVioletGlow.copy(alpha = 0.2f),
                            selectedLabelColor = SparkVioletGlow,
                            selectedLeadingIconColor = SparkVioletGlow
                        ),
                        modifier = Modifier.testTag("chip_deep_think")
                    )
                }

                // Input Bar Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(22.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Attachment button
                    IconButton(
                        onClick = { onNavigate(SparkScreen.DOCUMENT_ANALYSIS) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("home_attach_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach file or document",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Camera button
                    IconButton(
                        onClick = { onNavigate(SparkScreen.CAMERA_VISION) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("home_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera vision",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Text Input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        if (promptText.isEmpty()) {
                            Text(
                                text = "Ask SparkAI anything...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                        BasicTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(SparkCyan),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = { submitCurrentPrompt(promptText) }
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_prompt_input")
                        )
                    }

                    // Clear button if text is present
                    if (promptText.isNotBlank()) {
                        IconButton(
                            onClick = { promptText = "" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear input",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Dynamic Send / Live Voice Action Button with animated crossfade
                    Crossfade(
                        targetState = promptText.isNotBlank(),
                        label = "action_button_crossfade"
                    ) { hasText ->
                        if (hasText) {
                            IconButton(
                                onClick = { submitCurrentPrompt(promptText) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SparkCyan, CircleShape)
                                    .testTag("home_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Prompt",
                                    tint = Color(0xFF082F49),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { onNavigate(SparkScreen.LIVE_VOICE) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SparkVioletGlow.copy(alpha = 0.2f), CircleShape)
                                    .testTag("home_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Live Voice Conversation",
                                    tint = SparkVioletGlow,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = Modifier
            .width(150.dp)
            .height(130.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
