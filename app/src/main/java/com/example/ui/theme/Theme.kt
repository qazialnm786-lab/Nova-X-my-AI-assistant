package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaCyan,
    onPrimary = NovaBackground,
    primaryContainer = NovaSurfaceVariant,
    onPrimaryContainer = NovaCyan,
    secondary = NovaViolet,
    onSecondary = NovaTextPrimary,
    secondaryContainer = NovaSurfaceVariant,
    onSecondaryContainer = NovaViolet,
    tertiary = NovaMagenta,
    onTertiary = NovaTextPrimary,
    background = NovaBackground,
    onBackground = NovaTextPrimary,
    surface = NovaSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurfaceVariant,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaSurfaceBorder,
    outlineVariant = NovaSurfaceBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography = Typography,
        content = content
    )
}

