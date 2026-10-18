package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkRose
import com.example.ui.theme.SparkVioletGlow

@Composable
fun SparkComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onAttachClick: () -> Unit,
    isStreaming: Boolean = false,
    onStopStreaming: () -> Unit = {},
    enableSearchGrounding: Boolean = false,
    onToggleSearchGrounding: () -> Unit = {},
    enableThinking: Boolean = false,
    onToggleThinking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val hasText = text.isNotBlank()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Mode Chips Row (Search Grounding & High Thinking)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Search Grounding Chip
                FilterChip(
                    selected = enableSearchGrounding,
                    onClick = onToggleSearchGrounding,
                    label = { Text("Web Search", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web Search",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SparkCyan.copy(alpha = 0.2f),
                        selectedLabelColor = SparkCyan,
                        selectedLeadingIconColor = SparkCyan
                    )
                )

                // High Thinking Chip
                FilterChip(
                    selected = enableThinking,
                    onClick = onToggleThinking,
                    label = { Text("Deep Think", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Deep Thinking",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SparkVioletGlow.copy(alpha = 0.2f),
                        selectedLabelColor = SparkVioletGlow,
                        selectedLeadingIconColor = SparkVioletGlow
                    )
                )
            }

            // Input Row
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Attach button
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach file or photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Camera button
                IconButton(
                    onClick = onCameraClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take photo for vision",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Text Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Ask SparkAI anything...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(SparkCyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (hasText && !isStreaming) {
                                    onSend(text)
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isStreaming) {
                    // Stop Button
                    IconButton(
                        onClick = onStopStreaming,
                        modifier = Modifier
                            .size(40.dp)
                            .background(SparkRose, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop response",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (hasText) {
                    // Send Button
                    IconButton(
                        onClick = {
                            onSend(text)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(SparkCyan, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send prompt",
                            tint = Color(0xFF082F49),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Live Voice button
                    IconButton(
                        onClick = onVoiceClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(SparkVioletGlow.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Live Voice Mode",
                            tint = SparkVioletGlow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
