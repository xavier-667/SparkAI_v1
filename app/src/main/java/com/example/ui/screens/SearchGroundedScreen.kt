package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GroundingCitation
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkVioletGlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun SearchGroundedScreen(
    onSearchQuery: (String) -> Unit,
    onGroundingSearch: ((String) -> Flow<Pair<String, List<GroundingCitation>>>)? = null,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    var queryText by remember { mutableStateOf("Mars Sample Return mission 2026 update") }
    var isSearching by remember { mutableStateOf(false) }
    var groundedResult by remember {
        mutableStateOf(
            "NASA's Mars Sample Return architecture has been officially updated to incorporate accelerated dual-rotorcraft retrieval mechanisms [1]. Vacuum chamber tests conducted at the Jet Propulsion Laboratory [2] verified seal integrity for volatile organic samples collected in the ancient Jezero delta [3]."
        )
    }

    var citations by remember {
        mutableStateOf(
            listOf(
                GroundingCitation(1, "NASA Mars Exploration Update", "https://nasa.gov/perseverance-sample-return", "Revised mission architecture targets 2028 retrieval utilizing twin upgraded rotorcraft."),
                GroundingCitation(2, "Jet Propulsion Laboratory Analysis", "https://jpl.nasa.gov/missions/mars-sample-return", "Cache containment verification completes thermal testing in vacuum chamber simulation."),
                GroundingCitation(3, "Nature Astronomy Overview", "https://nature.com/articles/mars-sample-2026", "Scientific consensus highlights pristine sedimentary strata collected in Jezero crater.")
            )
        )
    }

    fun executeSearch() {
        if (queryText.isBlank() || isSearching) return
        if (onGroundingSearch != null) {
            coroutineScope.launch {
                isSearching = true
                groundedResult = "Connecting to Google Search indices and synthesizing verified evidence..."
                try {
                    onGroundingSearch(queryText).collect { (text, newCitations) ->
                        if (text.isNotEmpty()) groundedResult = text
                        if (newCitations.isNotEmpty()) citations = newCitations
                    }
                } catch (e: Exception) {
                    groundedResult = "Search error: ${e.message}"
                } finally {
                    isSearching = false
                }
            }
        } else {
            onSearchQuery(queryText)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        item {
            // Search Input Header
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SparkCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("Search grounded web intelligence...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { executeSearch() },
                        enabled = !isSearching
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = SparkCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Search",
                                tint = SparkCyan
                            )
                        }
                    }
                }
            }
        }

        // Live Grounding Status Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SparkCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GROUNDED VIA GOOGLE SEARCH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        TextButton(onClick = { onSearchQuery(queryText) }) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open in Chat", color = SparkCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = groundedResult,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // Citations List
        item {
            Text(
                text = "INDEXED SOURCES (${citations.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            )
        }

        items(citations) { citation ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            uriHandler.openUri(citation.url)
                        } catch (e: Exception) {
                            // Fallback if URL is invalid or browser unavailable
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SparkCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${citation.index}",
                                fontWeight = FontWeight.Bold,
                                color = SparkCyan,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = citation.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = citation.url,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SparkCyan,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.OpenInBrowser, contentDescription = "Open", tint = SparkCyan, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = citation.snippet,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
