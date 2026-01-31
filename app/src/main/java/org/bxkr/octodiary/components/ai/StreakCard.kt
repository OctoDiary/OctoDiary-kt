package org.bxkr.octodiary.components.ai


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.StreakManager

/**
 * Карточка стрика (серии дней)
 */
@Composable
fun StreakCard(type: String, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(type) {
        currentStreak = StreakManager.getCurrentStreak(context, type)
        longestStreak = StreakManager.getLongestStreak(context, type)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showInfoDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = if (currentStreak > 0) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    null,
                    Modifier.size(40.dp),
                    tint = if (currentStreak > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (currentStreak > 0) {
                            "$currentStreak ${getDayWord(currentStreak)} подряд 🔥"
                        } else {
                            "Стрик потерян"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentStreak > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            if (longestStreak > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Рекорд",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$longestStreak",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    // Диалог с информацией о стрике
    if (showInfoDialog) {
        StreakInfoDialog(
            type = type,
            title = title,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreakInfoDialog(
    type: String,
    title: String,
    currentStreak: Int,
    longestStreak: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Настройки стрика
    val prefs = context.getSharedPreferences("streak_settings", android.content.Context.MODE_PRIVATE)
    var goalDays by remember { mutableStateOf(prefs.getInt("${type}_goal", 7)) }
    var remindersEnabled by remember { mutableStateOf(prefs.getBoolean("${type}_reminders", true)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            when (type) {
                "homework" -> Icon(Icons.Default.CheckCircle, null)
                "diary" -> Icon(Icons.Default.Book, null)
                "ai_usage" -> Icon(Icons.Default.AutoAwesome, null)
                else -> Icon(Icons.Default.LocalFireDepartment, null)
            }
        },
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Объяснение стрика
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Что это?",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            getStreakExplanation(type),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Divider()
                
                // Статистика
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$currentStreak",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Текущий",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$longestStreak",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            "Рекорд",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Divider()
                
                // Настройки
                Text(
                    "Настройки",
                    style = MaterialTheme.typography.titleSmall
                )
                
                // Цель
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Цель (дней)")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = {
                            if (goalDays > 1) {
                                goalDays--
                                prefs.edit().putInt("${type}_goal", goalDays).apply()
                            }
                        }) {
                            Icon(Icons.Default.Remove, null)
                        }
                        Text(
                            "$goalDays",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = {
                            if (goalDays < 365) {
                                goalDays++
                                prefs.edit().putInt("${type}_goal", goalDays).apply()
                            }
                        }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
                
                // Напоминания
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Напоминания")
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = {
                            remindersEnabled = it
                            prefs.edit().putBoolean("${type}_reminders", it).apply()
                        }
                    )
                }
                
                // Прогресс к цели
                if (currentStreak > 0) {
                    Column {
                        Text(
                            "Прогресс к цели: $currentStreak / $goalDays",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = (currentStreak.toFloat() / goalDays.toFloat()).coerceAtMost(1f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

private fun getStreakExplanation(type: String): String {
    return when (type) {
        "homework" -> "Отслеживает сколько дней подряд ты выполняешь все домашние задания. Помогает выработать привычку делать ДЗ вовремя!"
        "diary" -> "Считает дни ведения дневника. Регулярное использование помогает оставаться организованным!"
        "ai_usage" -> "Показывает сколько дней подряд ты пользуешься AI помощником. Умный помощник делает учёбу эффективнее!"
        else -> "Следит за регулярностью выполнения задач. Чем длиннее стрик, тем лучше твои привычки!"
    }
}

private fun getDayWord(count: Int): String {
    val lastDigit = count % 10
    val lastTwoDigits = count % 100
    
    return when {
        lastTwoDigits in 11..19 -> "дней"
        lastDigit == 1 -> "день"
        lastDigit in 2..4 -> "дня"
        else -> "дней"
    }
}



