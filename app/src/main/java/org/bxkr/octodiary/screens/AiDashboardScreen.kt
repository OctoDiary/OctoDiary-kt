package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.ai.GeminiService
import org.bxkr.octodiary.ai.HomeworkAnalyzer
import org.bxkr.octodiary.components.ai.AutomaticRecordingStatusCard
import org.bxkr.octodiary.components.ai.GeneralAiChatDialog
import org.bxkr.octodiary.components.ai.PdfTextExtractorDialog
import org.bxkr.octodiary.components.ai.StreakCard
import org.bxkr.octodiary.components.ai.StudyPlanCard
import org.bxkr.octodiary.database.AppDatabase
import org.bxkr.octodiary.database.entity.KnowledgeBaseEntity
import org.bxkr.octodiary.models.ai.StudyPlan
import org.bxkr.octodiary.navControllerLive
import java.util.Date

/**
 * AI Дашборд - главный экран с аналитикой
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDashboardScreen() {
    val context = LocalContext.current
    val nav = navControllerLive.value
    val scope = rememberCoroutineScope()
    
    var studyPlan by remember { mutableStateOf<StudyPlan?>(null) }
    var isGeneratingPlan by remember { mutableStateOf(false) }
    var apiKeySet by remember { mutableStateOf(GeminiService.getApiKey(context).isNotEmpty()) }
    var showGeneralAiChat by remember { mutableStateOf(false) }
    var showPdfExtractor by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Помощник") },
                navigationIcon = {
                    IconButton(onClick = { nav?.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.Settings, "Настройки")
                    }
                }
            )
        }
    ) { padding ->
        if (!apiKeySet) {
            // Показать экран настройки API ключа
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Key,
                    null,
                    Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Настройте API ключ",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Для работы AI требуется API ключ Google Gemini",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                
                var apiKey by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API ключ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        GeminiService.setApiKey(context, apiKey)
                        apiKeySet = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Получите бесплатный ключ на ai.google.dev",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Заголовок
                item {
                    Text(
                        "Твоя статистика",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                
                // Стрики
                item {
                    Text(
                        "Стрики",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    StreakCard(
                        type = "homework",
                        title = "Домашние задания",
                        icon = Icons.Default.CheckCircle
                    )
                }
                item {
                    StreakCard(
                        type = "grades",
                        title = "Хорошие оценки",
                        icon = Icons.Default.Star
                    )
                }
                item {
                    StreakCard(
                        type = "study",
                        title = "Учёба",
                        icon = Icons.Default.MenuBook
                    )
                }
                
                // Персональный план
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Персональный план",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (DataService.hasHomeworks) {
                            TextButton(onClick = {
                                isGeneratingPlan = true
                                scope.launch {
                                    val result = HomeworkAnalyzer.generateStudyPlan(
                                        context,
                                        DataService.homeworks.filter { !it.isDone }
                                    )
                                    result.onSuccess { plan ->
                                        studyPlan = plan
                                    }
                                    isGeneratingPlan = false
                                }
                            }) {
                                Text("Сгенерировать")
                            }
                        }
                    }
                }
                
                if (isGeneratingPlan) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                                Text("Составляю план...")
                            }
                        }
                    }
                } else if (studyPlan != null) {
                    item {
                        StudyPlanCard(studyPlan!!)
                    }
                } else if (DataService.hasHomeworks) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Нет плана на сегодня")
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "AI составит персональный план с учётом времени сна и сложности заданий",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                
                // Автоматическая запись лекций
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Автоматическая запись",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    AutomaticRecordingStatusCard()
                }

                // Быстрые действия
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Быстрые действия",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { /* Открыть словарь */ },
                            Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Book, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Словарь")
                        }
                        FilledTonalButton(
                            onClick = { /* Открыть конспекты */ },
                            Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Mic, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Конспекты")
                        }
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = { nav?.navigate(org.bxkr.octodiary.Screen.TextbookExtractorScreen.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoStories, null)
                        Spacer(Modifier.width(8.dp))
                        Text("🔍 Извлечение из учебников")
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = { nav?.navigate(org.bxkr.octodiary.Screen.TextbooksScreen.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MenuBook, null)
                        Spacer(Modifier.width(8.dp))
                        Text("📚 Библиотека учебников")
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = { showPdfExtractor = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null)
                        Spacer(Modifier.width(8.dp))
                        Text("📄 Извлечь текст из PDF")
                    }
                }
                
                // Широкая кнопка общего AI помощника
                item {
                    Button(
                        onClick = { showGeneralAiChat = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.SmartToy, null, Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "🤖 Общий AI Помощник",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
    
    // Диалог общего AI чата
    if (showGeneralAiChat) {
        GeneralAiChatDialog(
            onDismiss = { showGeneralAiChat = false }
        )
    }

    // Диалог извлечения текста из PDF
    if (showPdfExtractor) {
        PdfTextExtractorDialog(
            onDismiss = { showPdfExtractor = false },
            onTextExtracted = { extractedText ->
                // Сохраняем извлеченный текст в базу знаний
                scope.launch {
                    try {
                        val knowledgeBaseItem = KnowledgeBaseEntity(
                            subject = "PDF Import",
                            topic = "Извлеченный текст",
                            type = "note",
                            title = "Текст из PDF документа",
                            content = extractedText,
                            source = "pdf_import",
                            createdAt = Date(),
                            updatedAt = Date(),
                            searchableText = extractedText
                        )
 
                                        // Сохраняем в базу данных
                                        AppDatabase.getInstance(context)?.knowledgeBaseDao()?.insert(knowledgeBaseItem)
                        // Показываем уведомление об успехе
                        android.widget.Toast.makeText(
                            context,
                            "Текст успешно извлечен и сохранен в базу знаний",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
 
                    } catch (e: Exception) {
                        android.util.Log.e("PdfExtractor", "Error saving to database", e)
                        android.widget.Toast.makeText(
                            context,
                            "Ошибка при извлечении текста: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }
}



