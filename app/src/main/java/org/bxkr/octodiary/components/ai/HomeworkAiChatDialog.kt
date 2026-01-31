package org.bxkr.octodiary.components.ai


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.models.homeworks2.Homework
import org.bxkr.octodiary.models.marklistsubject.Mark
import java.text.SimpleDateFormat
import java.util.*

data class AiContextData(
    val previousTopic: String?,
    val nextTopic: String?,
    val marksHistory: String?
)

/**
 * Получить контекст для AI: темы уроков и оценки
 */
private fun buildAiContext(homework: Homework): AiContextData {
    var previousTopic: String? = null
    var nextTopic: String? = null
    var marksHistory: String? = null
    
    // Получаем темы предыдущего и следующего уроков
    if (DataService.hasEventCalendar) {
        try {
            val homeworkDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(homework.date.substringBefore("T"))
            
            val lessons = DataService.eventCalendar
                .filter { it.subjectName == homework.subjectName }
                .sortedBy { it.startAt }
            
            val currentIndex = lessons.indexOfFirst { 
                val lessonDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(it.startAt.substringBefore("T"))
                lessonDate?.after(homeworkDate) == true || lessonDate == homeworkDate
            }
            
            if (currentIndex > 0) {
                previousTopic = lessons[currentIndex - 1].title
            }
            if (currentIndex < lessons.size - 1 && currentIndex >= 0) {
                nextTopic = lessons[currentIndex + 1].title
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeworkAiChat", "Ошибка получения тем: ${e.message}")
        }
    }
    
    // Получаем историю оценок по предмету
    if (DataService.hasMarksSubject) {
        try {
            val subjectMarks = DataService.marksSubject
                .find { it.subjectName == homework.subjectName }
            
            if (subjectMarks != null && subjectMarks.periods?.isNotEmpty() == true) {
                val currentPeriod = subjectMarks.periods?.lastOrNull()
                if (currentPeriod != null) {
                    val marks = currentPeriod.marks
                    
                    if (marks.isNotEmpty()) {
                        marksHistory = buildString {
                            appendLine("Оценки за текущий период:")
                            marks.takeLast(10).forEach { mark: Mark ->
                                val date = mark.date.substringBefore("T")
                                val value = mark.value
                                val weight = mark.weight.toString()
                                val controlFormName = mark.controlFormName
                                appendLine("- $date: $value (вес: $weight) - $controlFormName")
                            }
                            
                            val avg = marks.mapNotNull { m: Mark -> m.value.toIntOrNull() }.average()
                            if (!avg.isNaN()) {
                                appendLine("\nСредняя оценка: %.2f".format(avg))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeworkAiChat", "Ошибка получения оценок: ${e.message}")
        }
    }
    
    return AiContextData(previousTopic, nextTopic, marksHistory)
}

/**
 * Диалог AI чата для домашнего задания
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkAiChatDialog(
    homework: Homework,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Получаем дополнительные данные для AI контекста
    val aiContext = remember(homework) {
        buildAiContext(homework)
    }
    
    // Проверяем, включён ли AI
    val aiPrefs = context.getSharedPreferences("main_prefs", android.content.Context.MODE_PRIVATE)
    val aiEnabled = aiPrefs.getBoolean("ai_enabled", true)
    
    if (!aiEnabled) {
        // AI отключён - показываем сообщение
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("AI помощник отключён") },
            text = { Text("Включите AI помощник в настройках приложения") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Понятно")
                }
            }
        )
        return
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text("AI помощник")
                            Text(
                                homework.subjectName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Закрыть")
                        }
                    }
                )
            }
        ) { padding ->
            AiChatComponent(
                chatId = "homework_${homework.homeworkEntryStudentId}",
                systemPrompt = HomeworkAiHelper.generateSystemPrompt(
                    context = context,
                    homework = homework,
                    previousTopic = aiContext.previousTopic,
                    nextTopic = aiContext.nextTopic,
                    subjectMarksHistory = aiContext.marksHistory
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}



