package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkVioletGlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun CodeAssistantScreen(
    onOpenInCanvas: (String) -> Unit,
    onReviewCode: ((code: String, language: String, action: String) -> Flow<String>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val languages = listOf("Kotlin", "Python", "Rust", "TypeScript", "C++")
    var selectedLang by remember { mutableStateOf("Kotlin") }
    var isThinking by remember { mutableStateOf(false) }
    var assistantOutput by remember { mutableStateOf<String?>(null) }

    var userEditableCode by remember {
        mutableStateOf(
            """// Safe reactive event bus with coroutine channels
class SparkEventBus<T> {
    private val _events = Channel<T>(Channel.BUFFERED)
    val events: Flow<T> = _events.receiveAsFlow()

    suspend fun emit(event: T) {
        _events.send(event)
    }
}"""
        )
    }

    fun runCodeAction(action: String) {
        if (onReviewCode != null) {
            coroutineScope.launch {
                isThinking = true
                val accumulated = StringBuilder()
                try {
                    onReviewCode(userEditableCode, selectedLang, action).collect { chunk ->
                        accumulated.append(chunk)
                        assistantOutput = accumulated.toString()
                    }
                } catch (e: Exception) {
                    assistantOutput = "Code assistant error: ${e.message}"
                } finally {
                    isThinking = false
                }
            }
        } else {
            Toast.makeText(context, "Running $action analysis...", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Language Selector Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = {
                            selectedLang = lang
                            if (lang == "Python") {
                                userEditableCode = """# PyTorch Tensor Contraction
import torch

def contract_tensors(a: torch.Tensor, b: torch.Tensor) -> torch.Tensor:
    return torch.einsum('btd,bd->bt', a, b)"""
                            } else if (lang == "Kotlin") {
                                userEditableCode = """// Safe reactive event bus with coroutine channels
class SparkEventBus<T> {
    private val _events = Channel<T>(Channel.BUFFERED)
    val events: Flow<T> = _events.receiveAsFlow()

    suspend fun emit(event: T) {
        _events.send(event)
    }
}"""
                            }
                            assistantOutput = null
                        },
                        label = { Text(lang, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SparkCyan.copy(alpha = 0.2f),
                            selectedLabelColor = SparkCyan
                        )
                    )
                }
            }
        }

        // Code Editor & Viewer Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedLang • Gemini 3.1 Pro Code Engine",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SparkCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(userEditableCode))
                                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onOpenInCanvas(userEditableCode) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = "Open in Canvas", tint = SparkVioletGlow, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Editable Code Field with Monospace Styling
                    OutlinedTextField(
                        value = userEditableCode,
                        onValueChange = { userEditableCode = it },
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }

        // Action Buttons: Explain, Debug, Optimize
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { runCodeAction("EXPLAIN") },
                    enabled = !isThinking,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Explain", color = Color(0xFF082F49), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { runCodeAction("DEBUG") },
                    enabled = !isThinking,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SparkVioletGlow),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Find Bugs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { runCodeAction("OPTIMIZE") },
                    enabled = !isThinking,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Optimize & Diff", color = SparkCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Live Assistant Output Card
        if (assistantOutput != null || isThinking) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SparkCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isThinking) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SparkCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ANALYZING WITH GEMINI 3.1 PRO...", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SparkCyan))
                            } else {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GEMINI CODE ANALYSIS & DIFF", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SparkCyan))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = assistantOutput ?: "Synthesizing deep code analysis...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // Honest Capability Notice
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Static code inspection, algorithmic analysis, and diff generation are powered by Gemini 3.1 Pro. Isolated code compilation runtime is not executed on-device.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    )
                }
            }
        }
    }
}
