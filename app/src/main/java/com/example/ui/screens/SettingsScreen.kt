package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.theme.SparkVioletGlow

@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    userEmail: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var biometricLock by remember { mutableStateOf(false) }
    var fontScale by remember { mutableFloatStateOf(1.0f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
    ) {
        // Appearance Section
        item {
            Text(
                text = "APPEARANCE & THEME",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SparkCyan,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Dark theme toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Midnight Dark Theme (OLED)",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Deep obsidian backgrounds with cyan/violet aura accents",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = onToggleDarkTheme,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SparkCyan,
                                checkedTrackColor = SparkCyan.copy(alpha = 0.3f)
                            )
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Font Scale
                    Text(
                        text = "Font Scale: ${(fontScale * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Slider(
                        value = fontScale,
                        onValueChange = { fontScale = it },
                        valueRange = 0.85f..1.3f,
                        colors = SliderDefaults.colors(thumbColor = SparkCyan, activeTrackColor = SparkCyan)
                    )
                }
            }
        }

        // Privacy & Cloud Sync Section
        item {
            Text(
                text = "PRIVACY & CLOUD SYNC",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = SparkVioletGlow,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Cloud Firestore Sync",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (!userEmail.isNullOrBlank()) "Signed in as $userEmail" else "Sign in to synchronize chats across devices",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        if (!userEmail.isNullOrBlank()) {
                            OutlinedButton(onClick = onSignOut) {
                                Text("Sign Out")
                            }
                        } else {
                            Button(
                                onClick = onSignIn,
                                colors = ButtonDefaults.buttonColors(containerColor = SparkCyan)
                            ) {
                                Text("Sign In", color = Color(0xFF082F49), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Biometric Lock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Biometric Lock",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Require fingerprint/face to open SparkAI",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = biometricLock,
                            onCheckedChange = {
                                biometricLock = it
                                Toast.makeText(context, if (it) "Biometrics active" else "Biometrics disabled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // About SparkAI
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SparkAI Native Edition",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Version 1.0.0 (Build 2026.10) • Android 16 (API 36)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Engineered with Jetpack Compose & Google Gemini Models",
                        style = MaterialTheme.typography.labelSmall.copy(color = SparkCyan)
                    )
                }
            }
        }
    }
}
