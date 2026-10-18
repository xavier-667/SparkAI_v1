package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SparkCyan

data class SearchResultItem(
    val title: String,
    val snippet: String,
    val type: String, // "Chat", "Code", "Research", "Doc"
    val timestamp: String
)

@Composable
fun UniversalSearchScreen(
    onSelectResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("coroutine") }
    val filters = listOf("All", "Chat", "Code", "Research", "Docs")
    var selectedFilter by remember { mutableStateOf("All") }

    val allResults = listOf(
        SearchResultItem("Kotlin Coroutine Flow Buffers", "...Channel.BUFFERED prevents backpressure drops across high-frequency collector threads...", "Code", "2d ago"),
        SearchResultItem("Shor's Algorithm Quantum Principles", "...analyzing computational speedup over classical sub-exponential number field sieves...", "Chat", "1d ago"),
        SearchResultItem("Solid-State Battery Energy Density 2026", "...sulfide-based electrolyte solid-state cells achieved gravimetric densities exceeding 520 Wh/kg...", "Research", "3d ago"),
        SearchResultItem("Q3 Financial Audit Report", "...operating margins reached 31.5% with significant positive free cash flow expansion...", "Doc", "5d ago")
    )

    val filteredResults = allResults.filter {
        (selectedFilter == "All" || it.type == selectedFilter) &&
                (it.title.contains(searchQuery, ignoreCase = true) || it.snippet.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Search Input Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search chats, code, research & docs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SparkCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Filter Chips Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SparkCyan.copy(alpha = 0.2f),
                            selectedLabelColor = SparkCyan
                        )
                    )
                }
            }
        }

        // Results Count
        item {
            Text(
                text = "MATCHING RESULTS (${filteredResults.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            )
        }

        items(filteredResults) { item ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectResult(item.title) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SparkCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = item.type.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SparkCyan,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = item.timestamp,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.snippet,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}
