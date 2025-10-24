package org.bxkr.octodiary.components.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.models.BellSchedule
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Редактор кастомного расписания
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScheduleEditor(
    context: Context,
    onScheduleChanged: (List<BellSchedule>) -> Unit
) {
    val prefs = context.getSharedPreferences("bell_schedule_custom", Context.MODE_PRIVATE)
    
    // Загружаем сохранённое расписание
    var schedule by remember {
        mutableStateOf(loadCustomSchedule(prefs) ?: getDefaultCustomSchedule())
    }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingLesson by remember { mutableStateOf<Pair<Int, BellSchedule>?>(null) }
    
    LaunchedEffect(schedule) {
        // Сохраняем при изменении
        saveCustomSchedule(prefs, schedule)
        onScheduleChanged(schedule)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Кастомное расписание",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Rounded.Add, "Добавить урок")
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            if (schedule.isEmpty()) {
                Text(
                    "Нет уроков. Нажмите + чтобы добавить",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                
                schedule.forEachIndexed { index, lesson ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${lesson.lessonNumber} урок",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${lesson.startTime.format(formatter)} - ${lesson.endTime.format(formatter)} (${lesson.duration} мин)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (lesson.breakDuration > 0) {
                                Text(
                                    "Перемена: ${lesson.breakDuration} мин",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row {
                            IconButton(onClick = { editingLesson = index to lesson }) {
                                Icon(Icons.Rounded.Edit, "Редактировать", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                schedule = schedule.filterIndexed { i, _ -> i != index }
                                    .mapIndexed { newIndex, lesson ->
                                        lesson.copy(lessonNumber = newIndex + 1)
                                    }
                            }) {
                                Icon(Icons.Rounded.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    
                    if (index < schedule.size - 1) {
                        Divider(Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
    
    // Диалог добавления урока
    if (showAddDialog) {
        LessonEditDialog(
            lessonNumber = schedule.size + 1,
            onDismiss = { showAddDialog = false },
            onSave = { newLesson ->
                schedule = schedule + newLesson
                showAddDialog = false
            }
        )
    }
    
    // Диалог редактирования
    editingLesson?.let { (index, lesson) ->
        LessonEditDialog(
            lesson = lesson,
            onDismiss = { editingLesson = null },
            onSave = { updatedLesson ->
                schedule = schedule.toMutableList().apply {
                    this[index] = updatedLesson
                }
                editingLesson = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonEditDialog(
    lesson: BellSchedule? = null,
    lessonNumber: Int = 1,
    onDismiss: () -> Unit,
    onSave: (BellSchedule) -> Unit
) {
    var startHour by remember { mutableIntStateOf(lesson?.startTime?.hour ?: 8) }
    var startMinute by remember { mutableIntStateOf(lesson?.startTime?.minute ?: 30) }
    var duration by remember { mutableIntStateOf(lesson?.duration ?: 40) }
    var breakDuration by remember { mutableIntStateOf(lesson?.breakDuration ?: 20) }
    
    val endTime = LocalTime.of(startHour, startMinute).plusMinutes(duration.toLong())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (lesson != null) "Редактировать урок" else "Добавить урок") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Начало урока
                Text("Начало урока", style = MaterialTheme.typography.titleSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Часы
                    OutlinedTextField(
                        value = startHour.toString().padStart(2, '0'),
                        onValueChange = {
                            it.toIntOrNull()?.let { h ->
                                if (h in 0..23) startHour = h
                            }
                        },
                        label = { Text("ЧЧ") },
                        modifier = Modifier.width(80.dp)
                    )
                    Text(":", style = MaterialTheme.typography.headlineSmall)
                    // Минуты
                    OutlinedTextField(
                        value = startMinute.toString().padStart(2, '0'),
                        onValueChange = {
                            it.toIntOrNull()?.let { m ->
                                if (m in 0..59) startMinute = m
                            }
                        },
                        label = { Text("ММ") },
                        modifier = Modifier.width(80.dp)
                    )
                }
                
                // Длительность урока
                Text("Длительность урока: $duration мин", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = duration.toFloat(),
                    onValueChange = { duration = it.toInt() },
                    valueRange = 30f..60f,
                    steps = 5
                )
                
                // Перемена
                Text("Перемена после урока: $breakDuration мин", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = breakDuration.toFloat(),
                    onValueChange = { breakDuration = it.toInt() },
                    valueRange = 0f..30f,
                    steps = 5
                )
                
                // Превью
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        val formatter = DateTimeFormatter.ofPattern("HH:mm")
                        Text(
                            "Превью: ${LocalTime.of(startHour, startMinute).format(formatter)} - ${endTime.format(formatter)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newLesson = BellSchedule(
                    lessonNumber = lesson?.lessonNumber ?: lessonNumber,
                    startTime = LocalTime.of(startHour, startMinute),
                    endTime = endTime,
                    breakDuration = breakDuration
                )
                onSave(newLesson)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun getDefaultCustomSchedule(): List<BellSchedule> {
    // Возвращаем пустой список - пользователь сам настроит
    return emptyList()
}

private fun loadCustomSchedule(prefs: android.content.SharedPreferences): List<BellSchedule>? {
    val json = prefs.getString("schedule", null) ?: return null
    return try {
        val type = object : TypeToken<List<BellSchedule>>() {}.type
        Gson().fromJson(json, type)
    } catch (e: Exception) {
        null
    }
}

private fun saveCustomSchedule(prefs: android.content.SharedPreferences, schedule: List<BellSchedule>) {
    val json = Gson().toJson(schedule)
    prefs.edit().putString("schedule", json).apply()
}
