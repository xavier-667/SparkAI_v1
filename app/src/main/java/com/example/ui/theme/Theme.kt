package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SparkDarkColorScheme = darkColorScheme(
    primary = SparkCyan,
    onPrimary = Color(0xFF032238),
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = SparkVioletGlow,
    onSecondary = Color(0xFF3B0764),
    secondaryContainer = Color(0xFF581C87),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = SparkRose,
    onTertiary = Color(0xFF4C0519),
    tertiaryContainer = Color(0xFF881337),
    onTertiaryContainer = Color(0xFFFFE4E6),
    background = SparkDarkBg,
    onBackground = SparkTextPrimary,
    surface = SparkDarkSurface,
    onSurface = SparkTextPrimary,
    surfaceVariant = SparkDarkSurfaceVariant,
    onSurfaceVariant = SparkTextSecondary,
    surfaceContainer = SparkDarkSurfaceContainer,
    outline = SparkDarkOutline,
    outlineVariant = Color(0xFF1E293B)
)

val SparkLightColorScheme = lightColorScheme(
    primary = SparkCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF581C87),
    tertiary = SparkRose,
    onTertiary = Color.White,
    background = SparkLightBg,
    onBackground = SparkLightTextPrimary,
    surface = SparkLightSurface,
    onSurface = SparkLightTextPrimary,
    surfaceVariant = SparkLightSurfaceVariant,
    onSurfaceVariant = SparkLightTextSecondary,
    outline = SparkLightOutline
)

@Composable
fun SparkAITheme(
    darkTheme: Boolean = true, // Default to flagship dark midnight theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SparkDarkColorScheme else SparkLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
