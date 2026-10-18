package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkVioletGlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun DocumentScreen(
    onAnalyzePrompt: (String) -> Unit,
    onAnalyzeDocumentText: ((docName: String, content: String, query: String) -> Flow<String>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var docName by remember { mutableStateOf("Q3_Financial_Audit_Report.pdf") }
    var docSizeString by remember { mutableStateOf("4.2 MB") }
    var docContentText by remember {
        mutableStateOf(
            "Q3 2026 Financial Audit: Revenue grew 24.2% YoY to $384M. Operating margin expanded to 31.5% driven by serverless infrastructure efficiencies. Free cash flow achieved $142M."
        )
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    var userQuestion by remember { mutableStateOf("Summarize the key findings and risk factors from this document.") }
    var analysisResult by remember {
        mutableStateOf(
            "Document parsed successfully. You can ask specific questions about financial tables, technical specifications, or request an executive summary."
        )
    }

    // Document Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Get display name & size
                var fileName = "Document"
                var fileSize = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                // File size check: 10 MB maximum
                if (fileSize > 10 * 1024 * 1024) {
                    Toast.makeText(context, "File exceeds 10 MB maximum limit", Toast.LENGTH_LONG).show()
                    return@rememberLauncherForActivityResult
                }

                docName = fileName
                docSizeString = String.format("%.1f KB", fileSize / 1024.0)

                // Read text snippet
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val preview = StringBuilder()
                    var line: String?
                    var lineCount = 0
                    while (reader.readLine().also { line = it } != null && lineCount < 200) {
                        preview.append(line).append("\n")
                        lineCount++
                    }
                    docContentText = preview.toString()
                }

                analysisResult = "Attached $docName ($docSizeString). Ready for analysis."
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun runDocumentAnalysis(query: String) {
        if (onAnalyzeDocumentText != null) {
            coroutineScope.launch {
                isAnalyzing = true
                analysisResult = "Analyzing document with Gemini Pro..."
                val accumulated = StringBuilder()
                try {
                    onAnalyzeDocumentText(docName, docContentText, query).collect { chunk ->
                        accumulated.append(chunk)
                        analysisResult = accumulated.toString()
                    }
                } catch (e: Exception) {
                    analysisResult = "Analysis error: ${e.message}"
                } finally {
                    isAnalyzing = false
                }
            }
        } else {
            onAnalyzePrompt("Analyze $docName: $query\n\nContent snippet:\n$docContentText")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Document Attachment Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Document",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = docName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "$docSizeString • Gemini Multimodal Document Engine",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SparkCyan
                            )
                        )
                    }

                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Upload new",
                            tint = SparkCyan
                        )
                    }
                }
            }
        }

        // Extracted Core Metrics Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXTRACTED DOCUMENT METRICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SparkCyan,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBadge("Revenue YoY", "+24.2%", Color(0xFF10B981))
                        MetricBadge("Operating Margin", "31.5%", SparkCyan)
                        MetricBadge("Free Cash Flow", "$142M", SparkVioletGlow)
                    }
                }
            }
        }

        // Document Query Input & Actions
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DOCUMENT QUERY & QUESTION ANSWERING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SparkVioletGlow,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { runDocumentAnalysis(userQuestion) },
                        enabled = !isAnalyzing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF082F49))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Document...", fontWeight = FontWeight.Bold, color = Color(0xFF082F49))
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF082F49))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ask SparkAI About Document", fontWeight = FontWeight.Bold, color = Color(0xFF082F49))
                        }
                    }
                }
            }
        }

        // Suggested Actions
        item {
            Text(
                text = "ONE-TAP ACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {
                        userQuestion = "Summarize the primary strategic insights and risks."
                        runDocumentAnalysis(userQuestion)
                    },
                    label = { Text("Summarize Risks", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = {
                        userQuestion = "Extract all tabular and numerical data points into markdown table format."
                        runDocumentAnalysis(userQuestion)
                    },
                    label = { Text("Extract Tables", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Document Analysis Result Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANALYSIS & EXTRACTED INTELLIGENCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SparkCyan,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = analysisResult,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBadge(title: String, value: String, accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}
