package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkRose
import com.example.ui.theme.SparkVioletGlow

@Composable
fun ToolsScreen(
    onPingGateway: (suspend () -> Pair<Boolean, Long>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchEnabled by remember { mutableStateOf(true) }
    var visionEnabled by remember { mutableStateOf(true) }
    var imageGenEnabled by remember { mutableStateOf(true) }
    var mapsEnabled by remember { mutableStateOf(false) }
    var veoEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "AI CAPABILITIES & TOOLS REGISTRY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SparkCyan,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            ToolStatusCard(
                title = "Google Search Grounding",
                description = "Retrieves live web indices, breaking news, and citations via Gemini REST API (googleSearch).",
                icon = Icons.Default.Language,
                statusText = "LIVE & OPERATIONAL",
                isOperational = true,
                enabled = searchEnabled,
                onToggle = { searchEnabled = it }
            )
        }

        item {
            ToolStatusCard(
                title = "Gemini Multimodal Vision",
                description = "High-resolution camera and image analysis powered by gemini-3.5-flash with inline image payload.",
                icon = Icons.Default.Visibility,
                statusText = "LIVE & OPERATIONAL",
                isOperational = true,
                enabled = visionEnabled,
                onToggle = { visionEnabled = it }
            )
        }

        item {
            ToolStatusCard(
                title = "Gemini Image Studio",
                description = "Text-to-image synthesis powered by gemini-2.5-flash-image with custom aspect ratios.",
                icon = Icons.Default.Image,
                statusText = "LIVE & OPERATIONAL",
                isOperational = true,
                enabled = imageGenEnabled,
                onToggle = { imageGenEnabled = it }
            )
        }

        item {
            ToolStatusCard(
                title = "Python Sandbox Execution",
                description = "Static algorithmic inspection, debugging, and diffing. Native on-device compilation sandbox is not supported.",
                icon = Icons.Default.Terminal,
                statusText = "STATIC AI INSPECTION ONLY",
                isOperational = false,
                enabled = false,
                onToggle = { }
            )
        }

        item {
            ToolStatusCard(
                title = "Google Maps Grounding",
                description = "Real-world geospatial profiles and routes. Requires Enterprise Google Maps Platform enablement.",
                icon = Icons.Default.Place,
                statusText = "ENTERPRISE PREVIEW",
                isOperational = false,
                enabled = mapsEnabled,
                onToggle = { mapsEnabled = it }
            )
        }

        item {
            ToolStatusCard(
                title = "Veo 3.1 & Lyria 3 Audio",
                description = "Video and music synthesis. Requires long-running asynchronous operation polling via Vertex AI.",
                icon = Icons.Default.Movie,
                statusText = "ASYNCHRONOUS PREVIEW",
                isOperational = false,
                enabled = veoEnabled,
                onToggle = { veoEnabled = it }
            )
        }
    }
}

@Composable
private fun ToolStatusCard(
    title: String,
    description: String,
    icon: ImageVector,
    statusText: String,
    isOperational: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOperational) SparkCyan.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOperational) SparkCyan.copy(alpha = 0.15f) else SparkVioletGlow.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isOperational) SparkCyan else SparkVioletGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOperational) Color(0xFF10B981) else SparkRose)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isOperational) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                if (isOperational) {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SparkCyan,
                            checkedTrackColor = SparkCyan.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
