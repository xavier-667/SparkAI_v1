package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkRose
import com.example.ui.theme.SparkVioletGlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun CanvasWorkspaceScreen(
    initialContent: String = "",
    onRefineDocument: ((content: String, instruction: String) -> Flow<String>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var docContent by remember {
        mutableStateOf(
            if (initialContent.isNotBlank()) initialContent else
                """# SparkAI Architecture Specification
## 1. System Overview
SparkAI delivers full-featured native Android intelligence using Jetpack Compose, edge-to-edge window insets, and zero-trust Firestore synchronization.

## 2. Real-Time Streaming
Direct SSE streaming endpoints bypass intermediate buffering to achieve sub-200ms first-token latency on Gemini 3.1 Flash-Lite."""
        )
    }

    val historyStack = remember { mutableStateListOf<String>() }
    var isRefining by remember { mutableStateOf(false) }
    var customInstruction by remember { mutableStateOf("") }

    val wordCount = remember(docContent) {
        docContent.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    fun applyRefinement(instruction: String) {
        if (docContent.isBlank() || isRefining) return
        historyStack.add(docContent)

        if (onRefineDocument != null) {
            coroutineScope.launch {
                isRefining = true
                val accumulated = StringBuilder()
                try {
                    onRefineDocument(docContent, instruction).collect { chunk ->
                        accumulated.append(chunk)
                        docContent = accumulated.toString()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Refinement error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isRefining = false
                }
            }
        } else {
            Toast.makeText(context, "Refining: $instruction", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Document Meta Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Canvas Workspace & Writer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$wordCount words • ${docContent.length} chars • Saved",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            if (historyStack.isNotEmpty()) {
                                docContent = historyStack.removeLast()
                                Toast.makeText(context, "Reverted to previous version", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "No previous history to revert", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = historyStack.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = if (historyStack.isNotEmpty()) SparkCyan else MaterialTheme.colorScheme.outline)
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, docContent)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Canvas Document"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export", tint = SparkCyan)
                    }
                }
            }
        }

        // Quick AI Transformations Bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SPARKAI WRITING ASSISTANT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SparkVioletGlow,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { applyRefinement("Make this document more concise, direct, and eliminate filler words while preserving key facts.") },
                            label = { Text("Make Concise", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { applyRefinement("Add comprehensive technical rigor, scientific depth, and explicit architectural trade-offs.") },
                            label = { Text("Add Tech Depth", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { applyRefinement("Format as a formal Engineering RFC specification with clear goals, non-goals, and security review.") },
                            label = { Text("Format RFC", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customInstruction,
                            onValueChange = { customInstruction = it },
                            placeholder = { Text("Custom instruction (e.g., summarize risks)...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (customInstruction.isNotBlank()) {
                                    applyRefinement(customInstruction)
                                    customInstruction = ""
                                }
                            },
                            enabled = !isRefining
                        ) {
                            if (isRefining) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = SparkCyan)
                            } else {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = "Refine", tint = SparkCyan)
                            }
                        }
                    }
                }
            }
        }

        // Editable Canvas Document Editor
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = docContent,
                    onValueChange = { docContent = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }
    }
}
