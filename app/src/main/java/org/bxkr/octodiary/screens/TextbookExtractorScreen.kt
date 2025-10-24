package org.bxkr.octodiary.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.TextbookExtractorService
import org.bxkr.octodiary.components.ai.TextbookExtractCard
import org.bxkr.octodiary.components.ai.TextbookExtractInputCard
import org.bxkr.octodiary.database.entity.TextbookExtractEntity
import org.bxkr.octodiary.navControllerLive

/**
 * Экран для извлечения вопросов, упражнений и заданий из текста учебников с помощью AI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextbookExtractorScreen() {
    val context = LocalContext.current
    val nav = navControllerLive.value
    val scope = rememberCoroutineScope()

    var extracts by remember { mutableStateOf<List<TextbookExtractEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Загружаем существующие извлечения при запуске
    LaunchedEffect(Unit) {
        try {
            // Получаем все извлечения (для демонстрации)
            // В реальности можно фильтровать по параграфу или предмету
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Извлечение из учебников") },
                navigationIcon = {
                    IconButton(onClick = { nav?.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (extracts.isNotEmpty()) {
                        IconButton(onClick = {
                            // Очистить все извлечения
                            scope.launch {
                                try {
                                    // В будущем можно добавить метод очистки
                                    extracts = emptyList()
                                } catch (e: Exception) {
                                    errorMessage = "Ошибка очистки: ${e.message}"
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.Clear, "Очистить")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Карточка ввода текста
            item {
                TextbookExtractInputCard(
                    onExtract = { text ->
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                // Создаём временный параграф ID (в будущем можно будет выбирать существующий)
                                val paragraphId = System.currentTimeMillis()

                                val result = TextbookExtractorService.extractElements(
                                    context = context,
                                    paragraphId = paragraphId,
                                    text = text
                                )

                                result.onSuccess { newExtracts ->
                                    extracts = extracts + newExtracts
                                }.onFailure { error ->
                                    errorMessage = error.message ?: "Неизвестная ошибка"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Ошибка: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    isLoading = isLoading
                )
            }

            // Сообщение об ошибке
            errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Результаты извлечения
            if (extracts.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Найденные элементы",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${extracts.size} шт.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Группировка по типам
                val groupedExtracts = mapOf("placeholder" to extracts) // Placeholder

                groupedExtracts.forEach { (type, typeExtracts) ->
                    item {
                        Text(
                            text = when (type) {
                                "question" -> "📋 Вопросы (${typeExtracts.size})"
                                "exercise" -> "💪 Упражнения (${typeExtracts.size})"
                                "assignment" -> "📝 Задания (${typeExtracts.size})"
                                else -> "❓ Другое (${typeExtracts.size})"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(typeExtracts) { extract ->
                        TextbookExtractCard(
                            extract = extract,
                            onCompletionChanged = { completed ->
                                // Placeholder for updateCompletionStatus
                                // extracts = extracts.map {
                                //     if (it.id == extract.id) it.copy(isCompleted = completed)
                                //     else it
                                // }
                            }
                        )
                    }
                }
            } else if (!isLoading) {
                // Пустое состояние
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Извлеките вопросы, упражнения и задания",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Вставьте текст из учебника выше, и AI автоматически найдёт все учебные элементы",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }

            // Индикатор загрузки для новых извлечений
            if (isLoading) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                            Column {
                                Text(
                                    "Извлечение элементов...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "AI анализирует текст учебника",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}