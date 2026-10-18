package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryItem
import com.example.data.model.ProjectItem
import com.example.ui.theme.SparkCyan
import com.example.ui.theme.SparkVioletGlow

@Composable
fun ProjectsScreen(
    projects: List<ProjectItem>,
    memoryItems: List<MemoryItem>,
    onAddProject: (name: String, desc: String) -> Unit,
    onAddMemory: (key: String, value: String, category: String) -> Unit,
    onDeleteMemory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showNewMemoryDialog by remember { mutableStateOf(false) }

    var projectNameInput by remember { mutableStateOf("") }
    var projectDescInput by remember { mutableStateOf("") }

    var memoryKeyInput by remember { mutableStateOf("") }
    var memoryValInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Projects Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE PROJECTS (${projects.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SparkCyan,
                        letterSpacing = 1.sp
                    )
                )

                Button(
                    onClick = { showNewProjectDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF082F49), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF082F49))
                }
            }
        }

        // Project Items
        items(projects) { project ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SparkVioletGlow.copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = SparkVioletGlow, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${project.conversationsCount} chats • ${project.documentsCount} documents attached",
                            style = MaterialTheme.typography.labelSmall.copy(color = SparkCyan, fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // Contextual Long-Term Memory Section
        item {
            var memoryEnabled by remember { mutableStateOf(true) }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Inject Memory into Model Context",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (memoryEnabled) "Active: User preferences injected into prompts" else "Disabled: Isolated single-session prompts",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                    Switch(
                        checked = memoryEnabled,
                        onCheckedChange = {
                            memoryEnabled = it
                            Toast.makeText(context, if (it) "Memory context enabled" else "Memory context disabled", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SparkVioletGlow,
                            checkedTrackColor = SparkVioletGlow.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVED MEMORIES (${memoryItems.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SparkVioletGlow,
                        letterSpacing = 1.sp
                    )
                )

                TextButton(onClick = { showNewMemoryDialog = true }) {
                    Text("+ Add Memory", fontSize = 12.sp, color = SparkVioletGlow, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(memoryItems) { memory ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SparkVioletGlow.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = memory.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SparkVioletGlow
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = memory.key,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = memory.value,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(onClick = { onDeleteMemory(memory.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete memory", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = projectNameInput,
                        onValueChange = { projectNameInput = it },
                        label = { Text("Project Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = projectDescInput,
                        onValueChange = { projectDescInput = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectNameInput.isNotBlank()) {
                            onAddProject(projectNameInput, projectDescInput)
                            projectNameInput = ""
                            projectDescInput = ""
                            showNewProjectDialog = false
                            Toast.makeText(context, "Project created", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Memory Dialog
    if (showNewMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewMemoryDialog = false },
            title = { Text("Add Contextual Memory") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = memoryKeyInput,
                        onValueChange = { memoryKeyInput = it },
                        label = { Text("Key (e.g. Preferred Coding Style)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = memoryValInput,
                        onValueChange = { memoryValInput = it },
                        label = { Text("Detail / Directive") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memoryKeyInput.isNotBlank() && memoryValInput.isNotBlank()) {
                            onAddMemory(memoryKeyInput, memoryValInput, "Custom")
                            memoryKeyInput = ""
                            memoryValInput = ""
                            showNewMemoryDialog = false
                            Toast.makeText(context, "Memory item stored", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save Memory")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewMemoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
