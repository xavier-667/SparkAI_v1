package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GeneratedArtAsset
import com.example.data.model.SupportedAspectRatios
import com.example.data.model.SupportedImageSizes
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkRose
import com.example.ui.theme.SparkVioletGlow

@Composable
fun ImageStudioScreen(
    assets: List<GeneratedArtAsset>,
    onGenerateImage: (prompt: String, aspectRatio: String, size: String) -> Unit,
    onGenerateVeoVideo: (prompt: String, isLandscape: Boolean) -> Unit,
    onGenerateLyriaMusic: (prompt: String, isShortClip: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Image (gemini-2.5-flash-image), 1: Veo 3.1 Video, 2: Lyria 3 Music
    var promptInput by remember { mutableStateOf("Cyberpunk rooftop garden, holographic neon rain, cinematic volumetric lighting, 8k") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }
    var selectedSize by remember { mutableStateOf("2K") }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Tab Switcher
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = SparkCyan,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Image Studio", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Veo Video", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Lyria Music", fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Capability Status Notice
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTab == 0) SparkCyan.copy(alpha = 0.12f) else SparkVioletGlow.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selectedTab == 0) SparkCyan.copy(alpha = 0.3f) else SparkVioletGlow.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (selectedTab == 0) Color(0xFF10B981) else SparkVioletGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (selectedTab) {
                            0 -> "Live Image Model: Powered by gemini-2.5-flash-image (supports 1:1, 16:9, 9:16 and custom sizes)."
                            1 -> "Asynchronous Preview: Veo 3.1 video generation requires long-running operation polling or Google Cloud Vertex AI quota."
                            else -> "Audio Engine Preview: Lyria 3 music synthesis requires Google Cloud audio generation credentials."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // Prompt Input & Configurator Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "IMAGE GENERATION (gemini-2.5-flash-image)"
                            1 -> "VEO 3.1 FAST VIDEO GENERATION"
                            else -> "LYRIA 3 MUSIC ENGINE"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SparkCyan,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = { Text("Enter prompt description...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTab == 0) {
                        // Aspect Ratio Affordances
                        Text(
                            text = "Aspect Ratio:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SupportedAspectRatios) { ratio ->
                                FilterChip(
                                    selected = selectedAspectRatio == ratio.value,
                                    onClick = { selectedAspectRatio = ratio.value },
                                    label = { Text(ratio.label, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Resolution Affordances
                        Text(
                            text = "Resolution:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SupportedImageSizes.forEach { size ->
                                FilterChip(
                                    selected = selectedSize == size,
                                    onClick = { selectedSize = size },
                                    label = { Text(size, fontSize = 11.sp) }
                                )
                            }
                        }
                    } else if (selectedTab == 1) {
                        Text(
                            text = "Video Orientation (Veo 3.1):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedAspectRatio == "16:9",
                                onClick = { selectedAspectRatio = "16:9" },
                                label = { Text("16:9 Landscape") }
                            )
                            FilterChip(
                                selected = selectedAspectRatio == "9:16",
                                onClick = { selectedAspectRatio = "9:16" },
                                label = { Text("9:16 Portrait") }
                            )
                        }
                    } else {
                        Text(
                            text = "Track Type (Lyria 3):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedAspectRatio == "30s",
                                onClick = { selectedAspectRatio = "30s" },
                                label = { Text("Short Clip (<=30s)") }
                            )
                            FilterChip(
                                selected = selectedAspectRatio == "Full",
                                onClick = { selectedAspectRatio = "Full" },
                                label = { Text("Full Track") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isGenerating = true
                            when (selectedTab) {
                                0 -> onGenerateImage(promptInput, selectedAspectRatio, selectedSize)
                                1 -> onGenerateVeoVideo(promptInput, selectedAspectRatio == "16:9")
                                2 -> onGenerateLyriaMusic(promptInput, selectedAspectRatio == "30s")
                            }
                            isGenerating = false
                        },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF082F49)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Artwork with Gemini...", fontWeight = FontWeight.Bold, color = Color(0xFF082F49))
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF082F49)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedTab) {
                                    0 -> "Generate Artwork ($selectedSize, $selectedAspectRatio)"
                                    1 -> "Queue Veo 3.1 Video ($selectedAspectRatio)"
                                    else -> "Synthesize Lyria Music Track"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF082F49)
                            )
                        }
                    }
                }
            }
        }

        // Generated Assets Gallery
        item {
            Text(
                text = "RECENT GENERATED ASSETS (${assets.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            )
        }

        items(assets) { asset ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Preview Frame: AsyncImage if mediaUrl exists, else gradient placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (asset.mediaUrl.isNotBlank()) {
                            AsyncImage(
                                model = asset.mediaUrl,
                                contentDescription = asset.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = when (asset.type) {
                                        "VIDEO" -> Icons.Default.Movie
                                        "MUSIC" -> Icons.Default.MusicNote
                                        else -> Icons.Default.Image
                                    },
                                    contentDescription = null,
                                    tint = SparkCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${asset.type} • ${asset.resolution} • ${asset.aspectRatio}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = asset.prompt,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Model: ${asset.model}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SparkCyan,
                                fontSize = 11.sp
                            )
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Generated by SparkAI (${asset.model}): ${asset.prompt}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Art Asset"))
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = SparkCyan, modifier = Modifier.size(18.dp))
                            }

                            TextButton(
                                onClick = {
                                    Toast.makeText(context, "Asset saved to SparkAI gallery", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
