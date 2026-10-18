package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
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
import com.example.data.model.StudyQuizCard
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkRose
import com.example.ui.theme.SparkVioletGlow
import kotlinx.coroutines.launch

@Composable
fun StudyHubScreen(
    onAskTutor: (String) -> Unit,
    onGenerateDynamicQuiz: (suspend (String) -> StudyQuizCard)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var subjectTopicInput by remember { mutableStateOf("Quantum Computing") }
    var isGeneratingQuiz by remember { mutableStateOf(false) }

    var currentQuiz by remember {
        mutableStateOf(
            StudyQuizCard(
                id = "q1",
                subject = "Organic Chemistry",
                question = "What is the primary stereochemical consequence of a concerted bimolecular nucleophilic substitution (SN2) reaction at a chiral stereocenter?",
                options = listOf(
                    "Racemic mixture (50:50 retention and inversion)",
                    "Walden inversion (100% stereochemical inversion)",
                    "Complete retention of configuration via neighboring group participation",
                    "Elimination into a Hoffman alkene isomer"
                ),
                correctIndex = 1,
                explanation = "SN2 reactions occur through a backside attack into the antibonding σ* orbital, forcing the remaining three substituents to flip umbrella-like into an inverted stereocenter (Walden inversion)."
            )
        )
    }

    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }

    fun generateNewQuiz() {
        if (subjectTopicInput.isBlank() || isGeneratingQuiz) return
        if (onGenerateDynamicQuiz != null) {
            coroutineScope.launch {
                isGeneratingQuiz = true
                try {
                    val quiz = onGenerateDynamicQuiz(subjectTopicInput)
                    currentQuiz = quiz
                    selectedOption = null
                    showExplanation = false
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not generate quiz: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isGeneratingQuiz = false
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Study Header
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Study Mode: ${currentQuiz.subject}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SparkVioletGlow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Interactive",
                                style = MaterialTheme.typography.labelSmall.copy(color = SparkVioletGlow, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Practice questions generated by Gemini AI with step-by-step reasoning and worked examples.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Generate Quiz on Any Topic Input Bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = subjectTopicInput,
                        onValueChange = { subjectTopicInput = it },
                        placeholder = { Text("Subject (e.g. Kotlin, Physics)...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { generateNewQuiz() },
                        enabled = !isGeneratingQuiz,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SparkCyan)
                    ) {
                        if (isGeneratingQuiz) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF082F49))
                        } else {
                            Text("Generate", color = Color(0xFF082F49), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Question Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PRACTICE QUESTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SparkCyan,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQuiz.question,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    )
                }
            }
        }

        // Options List
        itemsIndexed(currentQuiz.options) { index, option ->
            val isSelected = selectedOption == index
            val isCorrect = index == currentQuiz.correctIndex
            val showStatus = selectedOption != null

            val backgroundColor = when {
                showStatus && isCorrect -> Color(0xFF10B981).copy(alpha = 0.15f)
                showStatus && isSelected && !isCorrect -> SparkRose.copy(alpha = 0.15f)
                isSelected -> SparkCyan.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val borderColor = when {
                showStatus && isCorrect -> Color(0xFF10B981)
                showStatus && isSelected && !isCorrect -> SparkRose
                isSelected -> SparkCyan
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = backgroundColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedOption = index
                        showExplanation = true
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = borderColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = ('A' + index).toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (showStatus && isCorrect) Color(0xFF10B981) else if (showStatus && isSelected) SparkRose else SparkCyan,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (showStatus && isCorrect) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    } else if (showStatus && isSelected) {
                        Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = SparkRose, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Explanation & Ask Tutor Card
        if (showExplanation) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SparkCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXPLANATION & REASONING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SparkCyan,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQuiz.explanation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onAskTutor("Explain step-by-step: ${currentQuiz.question}\nCorrect Answer: ${currentQuiz.options[currentQuiz.correctIndex]}\nExplanation: ${currentQuiz.explanation}")
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SparkCyan)
                        ) {
                            Text("Ask SparkAI Tutor for Deeper Walkthrough", color = Color(0xFF082F49), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Educational Content Disclaimer
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
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = SparkVioletGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Educational Disclaimer: Questions and explanations are dynamically synthesized by Gemini AI for practice purposes. Always cross-verify academic theorems and scientific constants with primary textbooks or accredited curricula.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
