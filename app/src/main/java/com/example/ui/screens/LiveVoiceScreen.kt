package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkRose
import com.example.ui.theme.SparkVioletGlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LiveVoiceScreen(
    onEndCall: () -> Unit,
    onGenerateVoiceReply: ((String) -> Flow<String>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isMuted by remember { mutableStateOf(false) }
    var selectedVoice by remember { mutableStateOf("Kore (Warm Female)") }
    var transcriptionText by remember { mutableStateOf("Tap the microphone or speak to start voice conversation...") }
    var isListening by remember { mutableStateOf(false) }
    var isThinking by remember { mutableStateOf(false) }
    var aiSpokenResponse by remember { mutableStateOf("") }

    // TTS engine initialization
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        ttsInstance = tts

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun speakText(text: String) {
        ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LiveVoiceUtterance")
    }

    // Speech Recognition Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenQuery = matches?.firstOrNull() ?: ""
            if (spokenQuery.isNotBlank()) {
                transcriptionText = spokenQuery
                if (onGenerateVoiceReply != null) {
                    coroutineScope.launch {
                        isThinking = true
                        val accumulated = StringBuilder()
                        try {
                            onGenerateVoiceReply(spokenQuery).collect { chunk ->
                                accumulated.append(chunk)
                                aiSpokenResponse = accumulated.toString()
                            }
                            speakText(accumulated.toString())
                        } catch (e: Exception) {
                            aiSpokenResponse = "Voice error: ${e.message}"
                        } finally {
                            isThinking = false
                        }
                    }
                } else {
                    speakText("I heard you say: $spokenQuery")
                }
            }
        }
    }

    // Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "SparkAI Voice is listening...")
            }
            isListening = true
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                isListening = false
                Toast.makeText(context, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice", Toast.LENGTH_LONG).show()
        }
    }

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "SparkAI Voice is listening...")
            }
            isListening = true
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                isListening = false
                Toast.makeText(context, "Speech recognition unavailable", Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Pulsing circle animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status & Voice Info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SparkCyan.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SparkCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isListening || isThinking) SparkRose else Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "LISTENING..." else if (isThinking) "SYNTHESIZING..." else "VOICE STUDIO • REAL-TIME TTS/STT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SparkCyan
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isListening) "Listening to your voice..." else if (isThinking) "SparkAI is thinking..." else "Voice conversation ready",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Engine: Android SpeechRecognizer + Jetpack TTS ($selectedVoice)",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Ambient Animated Glowing Pulse Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .padding(20.dp)
                .clickable { startListening() }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val baseRadius = size.minDimension / 2.6f

                // Outer aura rings
                drawCircle(
                    color = SparkVioletGlow.copy(alpha = 0.2f),
                    radius = baseRadius * pulseScale * 1.2f,
                    center = center
                )
                drawCircle(
                    color = SparkCyan.copy(alpha = 0.35f),
                    radius = baseRadius * pulseScale,
                    center = center
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SparkCyan, SparkVioletGlow),
                        center = center,
                        radius = baseRadius
                    ),
                    radius = baseRadius * 0.85f,
                    center = center
                )
            }

            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.AutoAwesome,
                contentDescription = "Tap to speak",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        // Live Real-Time Transcription Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VOICE INPUT & SPOKEN REPLY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SparkCyan,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"$transcriptionText\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                if (aiSpokenResponse.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SparkAI: $aiSpokenResponse",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SparkVioletGlow
                        ),
                        maxLines = 3
                    )
                }
            }
        }

        // Controls (Mute, End Call, Voice Mode)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp)
        ) {
            // Speak / Mic Button
            IconButton(
                onClick = { startListening() },
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isListening) SparkRose else SparkCyan,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Speak",
                    tint = if (isListening) Color.White else Color(0xFF082F49)
                )
            }

            // End Call Button
            IconButton(
                onClick = {
                    ttsInstance?.stop()
                    onEndCall()
                },
                modifier = Modifier
                    .size(68.dp)
                    .background(SparkRose, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Voice Switcher Button
            IconButton(
                onClick = {
                    selectedVoice = if (selectedVoice.contains("Kore")) "Puck (Energetic Male)" else "Kore (Warm Female)"
                    Toast.makeText(context, "Selected voice: $selectedVoice", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SpatialAudioOff,
                    contentDescription = "Change Voice",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
