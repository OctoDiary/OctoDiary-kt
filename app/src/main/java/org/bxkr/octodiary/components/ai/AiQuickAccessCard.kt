package org.bxkr.octodiary.components.ai


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.navControllerLive

/**
 * Карточка быстрого доступа к AI фичам на главном экране
 */
@Composable
fun AiQuickAccessCard() {
    val context = LocalContext.current
    val nav = navControllerLive.value
    
    // Проверяем, включён ли AI
    val aiPrefs = context.getSharedPreferences("main_prefs", android.content.Context.MODE_PRIVATE)
    val aiEnabled = aiPrefs.getBoolean("ai_enabled", true)
    
    if (!aiEnabled) return
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "AI Помощник",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = { nav?.navigate(Screen.AiDashboard.route) }) {
                    Text("Открыть")
                }
            }
            
            Text(
                "Умный чат, анализ ДЗ, персональный план, стрики",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            HorizontalDivider()
            
            // Быстрые действия
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { nav?.navigate(Screen.VocabularySmartScreen.route) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Book, null, Modifier.size(20.dp))
                        Text("Словарь", style = MaterialTheme.typography.labelSmall)
                    }
                }
                FilledTonalButton(
                    onClick = { nav?.navigate(Screen.LectureNotesScreen.route) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Mic, null, Modifier.size(20.dp))
                        Text("Конспекты", style = MaterialTheme.typography.labelSmall)
                    }
                }
                FilledTonalButton(
                    onClick = { nav?.navigate(Screen.AiDashboard.route) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Dashboard, null, Modifier.size(20.dp))
                        Text("Дашборд", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}



