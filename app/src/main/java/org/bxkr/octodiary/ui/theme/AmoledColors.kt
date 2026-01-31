package org.bxkr.octodiary.ui.theme


import androidx.compose.material.icons.Icons
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// AMOLED Pure Black Theme - для OLED экранов
// Чисто черный фон экономит батарею и даёт лучший контраст

val AmoledColorScheme = darkColorScheme(
    // Основные цвета
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFD0E4FF),
    
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = Color(0xFFC8E6C9),
    
    tertiary = Color(0xFFCE93D8),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFE1BEE7),
    
    // Ошибки
    error = Color(0xFFEF5350),
    errorContainer = Color(0xFFC62828),
    onError = Color(0xFF000000),
    onErrorContainer = Color(0xFFFFCDD2),
    
    // Фон - PURE BLACK для OLED
    background = Color(0xFF000000),
    onBackground = Color(0xFFE0E0E0),
    
    // Поверхности
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF0A0A0A), // очень тёмный серый вместо черного
    onSurfaceVariant = Color(0xFFBDBDBD),
    
    // Контейнеры поверхностей
    surfaceTint = Color(0xFF90CAF9),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF000000),
    
    // Границы и разделители
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF000000), // статус бар - чисто черный
    
    // Scrim для модальных окон
    scrim = Color(0xDE000000),
    
    // Surface containers - градации черного/серого для карточек
    surfaceBright = Color(0xFF1A1A1A),
    surfaceContainer = Color(0xFF0F0F0F),
    surfaceContainerHigh = Color(0xFF141414),
    surfaceContainerHighest = Color(0xFF1F1F1F),
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceDim = Color(0xFF000000),
    
    // Инверсия для primary
    inversePrimary = Color(0xFF1976D2)
)



