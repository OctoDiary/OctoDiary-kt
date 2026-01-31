package org.bxkr.octodiary.components.ai


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Power
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.ai.GeminiService

/**
 * Секция настроек AI в главных настройках
 */
@Composable
fun AiSettingsSection() {
    val context = LocalContext.current
    
    var apiKey by remember { mutableStateOf(GeminiService.getApiKey(context)) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var bedTime by remember {
        val prefs = context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE)
        mutableStateOf(prefs.getString("bed_time", "21:00") ?: "21:00")
    }
    var autoRecordLectures by remember {
        val prefs = context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE)
        mutableStateOf(prefs.getBoolean("auto_record_lectures", false))
    }
    
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "AI Помощник",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // API ключ
        ListItem(
            headlineContent = { Text("API ключ Gemini") },
            supportingContent = {
                Text(
                    if (apiKey.isEmpty()) "Не установлен" else "Установлен"
                )
            },
            leadingContent = {
                Icon(Icons.Default.Key, null)
            },
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                TextButton(onClick = { showApiKeyDialog = true }) {
                    Text("Изменить")
                }
            }
        )
        
        HorizontalDivider()
        
        // Время сна
        ListItem(
            headlineContent = { Text("Время отхода ко сну") },
            supportingContent = { Text("Для составления плана") },
            leadingContent = {
                Icon(Icons.Default.Bedtime, null)
            },
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                Text(bedTime, style = MaterialTheme.typography.bodyLarge)
            }
        )
        
        HorizontalDivider()
        
        // Автозапись лекций
        ListItem(
            headlineContent = { Text("Автозапись уроков") },
            supportingContent = { Text("Создавать конспекты автоматически") },
            leadingContent = {
                Icon(Icons.Default.Mic, null)
            },
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                Switch(
                    checked = autoRecordLectures,
                    onCheckedChange = {
                        autoRecordLectures = it
                        val prefs = context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("auto_record_lectures", it).apply()

                        // Управляем сервисом при изменении настройки
                        if (it) {
                            org.bxkr.octodiary.audio.AutomaticLectureRecordingService.startAutomaticRecording(context)
                        } else {
                            org.bxkr.octodiary.audio.AutomaticLectureRecordingService.stopAutomaticRecording(context)
                        }
                    }
                )
            }
        )

        // Дополнительные настройки автозаписи
        if (autoRecordLectures) {
            val batteryLevel by remember {
                val prefs = context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE)
                mutableStateOf(prefs.getInt("auto_record_min_battery", 20))
            }
            val requireCharging by remember {
                val prefs = context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE)
                mutableStateOf(prefs.getBoolean("auto_record_require_charging", false))
            }

            ListItem(
                headlineContent = { Text("Мин. уровень батареи") },
                supportingContent = { Text("${batteryLevel}%") },
                leadingContent = {
                    Icon(Icons.Default.BatteryStd, null)
                },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    var showBatteryDialog by remember { mutableStateOf(false) }
                    TextButton(onClick = { showBatteryDialog = true }) {
                        Text("Изменить")
                    }

                    if (showBatteryDialog) {
                        var newBatteryLevel by remember { mutableStateOf(batteryLevel.toString()) }
                        AlertDialog(
                            onDismissRequest = { showBatteryDialog = false },
                            title = { Text("Мин. уровень батареи") },
                            text = {
                                OutlinedTextField(
                                    value = newBatteryLevel,
                                    onValueChange = {
                                        newBatteryLevel = it.filter { char -> char.isDigit() }
                                    },
                                    label = { Text("Процент (10-100)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val level = newBatteryLevel.toIntOrNull()?.coerceIn(10, 100) ?: 20
                                    context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE)
                                        .edit().putInt("auto_record_min_battery", level).apply()
                                    showBatteryDialog = false
                                }) {
                                    Text("Сохранить")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showBatteryDialog = false }) {
                                    Text("Отмена")
                                }
                            }
                        )
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Требуется зарядка") },
                supportingContent = { Text("Записывать только при зарядке") },
                leadingContent = {
                    Icon(Icons.Default.Power, null)
                },
                modifier = Modifier.fillMaxWidth(),
                trailingContent = {
                    Switch(
                        checked = requireCharging,
                        onCheckedChange = {
                            context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE)
                                .edit().putBoolean("auto_record_require_charging", it).apply()
                        }
                    )
                }
            )
        }
    }
    
    // Диалог изменения API ключа
    if (showApiKeyDialog) {
        var newKey by remember { mutableStateOf(apiKey) }
        
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("API ключ Gemini") },
            text = {
                Column {
                    Text("Получите бесплатный ключ на ai.google.dev")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("API ключ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    GeminiService.setApiKey(context, newKey)
                    apiKey = newKey
                    showApiKeyDialog = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}



