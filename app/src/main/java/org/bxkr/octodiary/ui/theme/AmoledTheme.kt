package org.bxkr.octodiary.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Pure black AMOLED theme for OLED displays
 * Saves battery and looks amazing
 */
val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9), // Light Blue
    onPrimary = Color(0xFF000000), // Pure Black
    primaryContainer = Color(0xFF121212), // Almost Black
    onPrimaryContainer = Color(0xFFBBDEFB),
    
    secondary = Color(0xFFCE93D8), // Light Purple
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF1A1A1A),
    onSecondaryContainer = Color(0xFFE1BEE7),
    
    tertiary = Color(0xFFA5D6A7), // Light Green
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF0D0D0D),
    onTertiaryContainer = Color(0xFFC8E6C9),
    
    error = Color(0xFFEF5350),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF1A0000),
    onErrorContainer = Color(0xFFFFCDD2),
    
    background = Color(0xFF000000), // Pure Black Background
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF000000), // Pure Black Surface
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF0A0A0A), // Slightly lighter than pure black
    onSurfaceVariant = Color(0xFFBDBDBD),
    
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF000000),
    
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFF1976D2),
    
    surfaceTint = Color(0xFF90CAF9),
    scrim = Color(0xFF000000)
)
