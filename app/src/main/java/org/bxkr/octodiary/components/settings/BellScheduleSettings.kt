package org.bxkr.octodiary.components.settings


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save
import org.bxkr.octodiary.workers.BellScheduleWorker
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BellScheduleSettings() {
    val activity = LocalActivity.current
    
    // Включен ли режим
    val bellScheduleEnabled = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("bell_schedule_enabled") ?: false) 
    }
    
    // Тип расписания
    val scheduleType = remember { 
        mutableStateOf(activity.mainPrefs.get<String>("bell_schedule_type") ?: "standard") 
    }
    
    // Уведомление перед уроком
    val notifyBefore = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("bell_schedule_notify_before") ?: true) 
    }
    
    // Минуты до урока
    val minutesBefore = remember { 
        mutableIntStateOf(activity.mainPrefs.get<Int>("bell_schedule_minutes_before") ?: 5) 
    }
    
    // Показывать обратный отсчёт
    val showCountdown = remember { 
        mutableStateOf(activity.mainPrefs.get<Boolean>("bell_schedule_show_countdown") ?: true) 
    }
    
    // Dropdown для типа расписания
    var scheduleExpanded by remember { mutableStateOf(false) }
    
    val scheduleTypes = listOf(
        "standard" to "Стандартное (45 мин, 8:30)",
        "short" to "Короткие уроки (40 мин, 8:30)",
        "second_shift" to "Вторая смена (45 мин, 13:30)",
        "custom" to "Кастомное расписание"
    )
    
    Column(Modifier.padding(vertical = 8.dp)) {
        
        // Заголовок
        Text(
            "Расписание звонков",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Включение расписания
        SwitchPreference(
            title = "Расписание звонков",
            description = "Уведомления о начале и конце уроков с обратным отсчётом",
            listenState = bellScheduleEnabled
        ) {
            bellScheduleEnabled.value = it
            activity.mainPrefs.save("bell_schedule_enabled" to it)
            
            // Запускаем/останавливаем worker
            if (it) {
                BellScheduleWorker.scheduleNotifications(activity)
            }
        }
        
        androidx.compose.animation.AnimatedVisibility(visible = bellScheduleEnabled.value) {
            Column {
                Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                // Тип расписания
                Text(
                    "Тип расписания",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = scheduleExpanded,
                    onExpandedChange = { scheduleExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = scheduleTypes.find { it.first == scheduleType.value }?.second ?: "Не выбрано",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scheduleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = scheduleExpanded,
                        onDismissRequest = { scheduleExpanded = false }
                    ) {
                        scheduleTypes.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    scheduleType.value = key
                                    activity.mainPrefs.save("bell_schedule_type" to key)
                                    scheduleExpanded = false
                                    BellScheduleWorker.scheduleNotifications(activity)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Редактор кастомного расписания
                if (scheduleType.value == "custom") {
                    CustomScheduleEditor(
                        context = activity,
                        onScheduleChanged = {
                            // Обновляем worker при изменении расписания
                            BellScheduleWorker.scheduleNotifications(activity)
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }
                
                // Превью расписания
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Превью расписания",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        val schedule = when (scheduleType.value) {
                            "standard" -> org.bxkr.octodiary.models.DefaultBellSchedules.STANDARD
                            "short" -> org.bxkr.octodiary.models.DefaultBellSchedules.SHORT
                            "second_shift" -> org.bxkr.octodiary.models.DefaultBellSchedules.SECOND_SHIFT
                            "custom" -> {
                                // Загружаем кастомное расписание
                                val prefs = activity.getSharedPreferences("bell_schedule_custom", android.content.Context.MODE_PRIVATE)
                                val json = prefs.getString("schedule", null)
                                if (json != null) {
                                    try {
                                        val type = object : com.google.gson.reflect.TypeToken<List<org.bxkr.octodiary.models.BellSchedule>>() {}.type
                                        com.google.gson.Gson().fromJson(json, type) ?: emptyList()
                                    } catch (e: Exception) {
                                        emptyList()
                                    }
                                } else {
                                    emptyList()
                                }
                            }
                            else -> org.bxkr.octodiary.models.DefaultBellSchedules.STANDARD
                        }
                        
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        
                        schedule.take(4).forEach { lesson ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${lesson.lessonNumber} урок",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${lesson.startTime.format(formatter)} - ${lesson.endTime.format(formatter)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        if (schedule.size > 4) {
                            Text(
                                "И ещё ${schedule.size - 4} уроков...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                Divider(Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
                
                // Настройки уведомлений
                Text(
                    "Уведомления",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                SwitchPreference(
                    title = "Напоминать перед уроком",
                    description = "Уведомление за несколько минут до начала",
                    listenState = notifyBefore
                ) {
                    notifyBefore.value = it
                    activity.mainPrefs.save("bell_schedule_notify_before" to it)
                }
                
                if (notifyBefore.value) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "За сколько минут",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "${minutesBefore.intValue} мин",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Slider(
                            value = minutesBefore.intValue.toFloat(),
                            onValueChange = { minutesBefore.intValue = it.toInt() },
                            onValueChangeFinished = {
                                activity.mainPrefs.save("bell_schedule_minutes_before" to minutesBefore.intValue)
                            },
                            valueRange = 1f..15f,
                            steps = 13,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                SwitchPreference(
                    title = "Обратный отсчёт",
                    description = "Показывать постоянное уведомление с таймером",
                    listenState = showCountdown
                ) {
                    showCountdown.value = it
                    activity.mainPrefs.save("bell_schedule_show_countdown" to it)
                }
                
                Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                // Информация
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                "О расписании звонков",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Уведомления помогут не опоздать на урок и узнать, сколько времени осталось до конца. " +
                                "Обратный отсчёт показывается в постоянном уведомлении во время урока. " +
                                "Вы можете выбрать один из трёх типов расписания или настроить своё.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}



