package org.bxkr.octodiary.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.PdfProcessingProgress
import org.bxkr.octodiary.managers.TocManager
import org.bxkr.octodiary.managers.TextbookManager
// import org.bxkr.octodiary.models.TocEntry

/**
 * Компонент для просмотра оглавления учебника
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TocViewer(
    textbookId: String,
    onEntryClick: (Any) -> Unit = {},
    onGenerateToc: (suspend (PdfProcessingProgress) -> Unit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tocEntries by remember { mutableStateOf<List<Any>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasToc by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredEntries by remember { mutableStateOf<List<Any>>(emptyList()) }

    // Состояние прогресса генерации
    var isGenerating by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf<PdfProcessingProgress?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) }

    // Загружаем оглавление при запуске
    LaunchedEffect(textbookId) {
        isLoading = true
        try {
            hasToc = TocManager.hasTocForTextbook(context, textbookId)
            if (hasToc) {
                tocEntries = TocManager.getTocForTextbook(context, textbookId)
                filteredEntries = tocEntries
            }
        } catch (e: Exception) {
            android.util.Log.e("TocViewer", "Ошибка загрузки оглавления", e)
        } finally {
            isLoading = false
        }
    }

    // Фильтруем по поисковому запросу
    LaunchedEffect(searchQuery, tocEntries) {
        filteredEntries = if (searchQuery.isBlank()) {
            tocEntries
        } else {
            scope.launch {
                try {
                    filteredEntries = TocManager.searchInToc(context, textbookId, searchQuery)
                } catch (e: Exception) {
                    filteredEntries = tocEntries.filter { entry ->
                        // Placeholder for filtering logic without TocEntry
                        // For now, return all entries if search is active, or empty list
                        true
                    }
                }
            }
            filteredEntries
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Заголовок и поиск
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Оглавление",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (hasToc) {
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                TocManager.clearTocForTextbook(context, textbookId)
                                hasToc = false
                                tocEntries = emptyList()
                                filteredEntries = emptyList()
                            } catch (e: Exception) {
                                android.util.Log.e("TocViewer", "Ошибка удаления оглавления", e)
                            }
                        }
                    }) {
                        Icon(Icons.Rounded.Delete, "Удалить оглавление")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (hasToc) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск по оглавлению...") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = "Поиск")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Прогресс-бар генерации
        if (isGenerating && currentProgress != null) {
            val progress = currentProgress!!
            val animatedProgress by animateFloatAsState(
                targetValue = progress.progress,
                label = "progress"
            )
            val progressBarColor by animateColorAsState(
                targetValue = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                label = "progressColor"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Прогресс-бар
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = progressBarColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Информация о странице и кнопка паузы
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${progress.currentPage}/${progress.totalPages}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { isPaused = !isPaused },
                        enabled = progress.canResume
                    ) {
                        Icon(
                            if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                            contentDescription = if (isPaused) "Возобновить" else "Приостановить"
                        )
                    }
                }

                // Сообщение об ошибке
                if (hasError && errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Содержимое
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !hasToc -> {
                // Нет оглавления - показываем кнопку генерации
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Оглавление не сгенерировано",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "AI может проанализировать PDF учебника и создать структурированное оглавление с заголовками и ключевыми темами.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isGenerating = true
                            hasError = false
                            errorMessage = ""
                            scope.launch {
                                try {
                                    val textbook = TextbookManager.getAllTextbooks(context).find { it.id == textbookId }
                                    if (textbook != null) {
                                        val result = TextbookManager.generateTocForTextbook(context, textbook) { progress ->
                                            currentProgress = progress
                                            hasError = progress.isPaused && !progress.canResume
                                            if (hasError) {
                                                errorMessage = progress.message ?: "Произошла ошибка"
                                            }
                                        }
                                        result.onSuccess {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Оглавление создано",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure { error ->
                                            android.widget.Toast.makeText(
                                                context,
                                                "Ошибка: ${error.message}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } finally {
                                    isGenerating = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        enabled = !isGenerating
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isGenerating) "Создание..." else "Создать оглавление")
                    }

                    // Кнопка продолжения с последнего сохранения
                    if (hasToc) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                isGenerating = true
                                hasError = false
                                errorMessage = ""
                                scope.launch {
                                    try {
                                        val textbook = TextbookManager.getAllTextbooks(context).find { it.id == textbookId }
                                        if (textbook != null) {
                                            // TODO: Реализовать продолжение с последнего сохранения
                                            // Пока просто перезапускаем генерацию
                                            val result = TextbookManager.generateTocForTextbook(context, textbook) { progress ->
                                                currentProgress = progress
                                                hasError = progress.isPaused && !progress.canResume
                                                if (hasError) {
                                                    errorMessage = progress.message ?: "Произошла ошибка"
                                                }
                                            }
                                            result.onSuccess {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Оглавление создано",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }.onFailure { error ->
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Ошибка: ${error.message}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } finally {
                                        isGenerating = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            enabled = !isGenerating
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Продолжить с последнего сохранения")
                        }
                    }
                }
            }

            filteredEntries.isEmpty() && searchQuery.isNotBlank() -> {
                // Ничего не найдено
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Ничего не найдено",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Попробуйте изменить поисковый запрос",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                // Показываем оглавление
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredEntries) { entry ->
                        TocEntryItem(
                            entry = entry,
                            onClick = { onEntryClick(entry) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Элемент оглавления
 */
@Composable
private fun TocEntryItem(
    entry: Any,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Placeholder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Заголовок", // Placeholder
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // if (entry.pageNumber != null) { // Placeholder
                //     Text(
                //         text = "стр. ${entry.pageNumber}",
                //         style = MaterialTheme.typography.bodySmall,
                //         color = MaterialTheme.colorScheme.onSurfaceVariant
                //     )
                // }
            }

            // if (entry.summary != null && entry.summary.isNotBlank()) { // Placeholder
            //     Spacer(modifier = Modifier.height(4.dp))
            //     Text(
            //         text = entry.summary,
            //         style = MaterialTheme.typography.bodySmall,
            //         color = MaterialTheme.colorScheme.onSurfaceVariant,
            //         maxLines = 2,
            //         overflow = TextOverflow.Ellipsis
            //     )
            // }

            // if (entry.keywords.isNotEmpty()) { // Placeholder
            //     Spacer(modifier = Modifier.height(4.dp))
            //     Row(
            //         horizontalArrangement = Arrangement.spacedBy(4.dp),
            //         verticalAlignment = Alignment.CenterVertically
            //     ) {
            //         Icon(
            //             Icons.Rounded.Tag,
            //             contentDescription = null,
            //             modifier = Modifier.size(12.dp),
            //             tint = MaterialTheme.colorScheme.onSurfaceVariant
            //         )
            //         Text(
            //             text = "Ключевые слова", // Placeholder
            //             style = MaterialTheme.typography.bodySmall,
            //             color = MaterialTheme.colorScheme.onSurfaceVariant,
            //             maxLines = 1,
            //             overflow = TextOverflow.Ellipsis
            //         )
            //     }
            // }
        }
    }
}