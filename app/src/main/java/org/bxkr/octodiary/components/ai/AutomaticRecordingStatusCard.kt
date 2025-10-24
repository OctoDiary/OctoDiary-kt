package org.bxkr.octodiary.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.audio.AutomaticLectureRecordingService

/**
 * Карточка статуса автоматической записи лекций
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomaticRecordingStatusCard() {
    val context = LocalContext.current
    val aiPrefs = remember { context.getSharedPreferences("ai_prefs", android.content.Context.MODE_PRIVATE) }

    var autoRecordEnabled by remember {
        mutableStateOf(aiPrefs.getBoolean("auto_record_lectures", false))
    }
    var isServiceRunning by remember { mutableStateOf(false) }
    var currentRecordingSubject by remember { mutableStateOf<String?>(null) }

    // Проверяем статус сервиса
    LaunchedEffect(Unit) {
        // TODO: Реализовать проверку статуса сервиса через BroadcastReceiver или другой механизм
        // Пока что показываем статус на основе настроек
        isServiceRunning = autoRecordEnabled
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isServiceRunning) Icons.Rounded.Mic else Icons.Rounded.MicOff,
                    contentDescription = null,
                    tint = if (isServiceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            !autoRecordEnabled -> "Автозапись отключена"
                            isServiceRunning -> "Автозапись активна"
                            else -> "Автозапись готова"
                        },
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = if (isServiceRunning && currentRecordingSubject != null) {
                            "Записывается: $currentRecordingSubject"
                        } else {
                            "Автоматическая запись уроков по расписанию"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Информация о настройках
            if (autoRecordEnabled) {
                val batteryLevel = aiPrefs.getInt("auto_record_min_battery", 20)
                val requireCharging = aiPrefs.getBoolean("auto_record_require_charging", false)

                Text(
                    text = "Настройки: минимум ${batteryLevel}% батареи" +
                           if (requireCharging) ", требуется зарядка" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Кнопки управления
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (isServiceRunning) {
                    OutlinedButton(
                        onClick = {
                            AutomaticLectureRecordingService.stopAutomaticRecording(context)
                            isServiceRunning = false
                            currentRecordingSubject = null
                        }
                    ) {
                        Icon(Icons.Rounded.Stop, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Остановить")
                    }
                } else {
                    Button(
                        onClick = {
                            AutomaticLectureRecordingService.startAutomaticRecording(context)
                            isServiceRunning = true
                        },
                        enabled = autoRecordEnabled
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Запустить")
                    }
                }
            }
        }
    }
}