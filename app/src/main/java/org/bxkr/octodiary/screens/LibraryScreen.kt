package org.bxkr.octodiary.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.StructuredText
import org.bxkr.octodiary.ai.StructuredParagraph
import org.bxkr.octodiary.ai.PdfTextExtractor

data class TextbookItem(
    val id: Int,
    val title: String,
    val subject: String,
    val author: String,
    val filePath: String,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val structuredText: StructuredText? = null,
    val uri: Uri? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextbookItemCard(
    item: TextbookItem,
    onStructureClick: () -> Unit,
    onViewStructure: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text("${item.subject} • ${item.author}", style = MaterialTheme.typography.bodySmall)
                    Text("Страница ${item.currentPage}/${item.totalPages}", style = MaterialTheme.typography.bodySmall)
                }

                if (item.structuredText != null) {
                    IconButton(onClick = onViewStructure) {
                        Icon(Icons.Rounded.Visibility, "Просмотр структуры")
                    }
                } else {
                    IconButton(onClick = onStructureClick) {
                        Icon(Icons.Rounded.AutoAwesome, "Структурировать AI")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var textbooks by remember { mutableStateOf<List<TextbookItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<TextbookItem?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingText by remember { mutableStateOf("") }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isProcessing = true
                processingText = "Извлечение текста из PDF..."

                // Извлекаем базовую информацию о PDF
                val title = "Учебник ${textbooks.size + 1}" // Можно улучшить, извлекая из метаданных
                val newItem = TextbookItem(
                    id = textbooks.size + 1,
                    title = title,
                    subject = "Неизвестно", // Можно определить через AI
                    author = "Неизвестно",
                    filePath = uri.toString(),
                    uri = uri
                )

                textbooks = textbooks + newItem
                isProcessing = false
            }
        }
    }

    fun structureTextbook(item: TextbookItem) {
        item.uri?.let { uri ->
            scope.launch {
                isProcessing = true
                processingText = "Структурирование текста через AI..."

                val result = PdfTextExtractor.structureTextWithAI(context, uri)
                result.onSuccess { structuredText ->
                    // Обновляем элемент с структурированным текстом
                    textbooks = textbooks.map {
                        if (it.id == item.id) {
                            it.copy(
                                structuredText = structuredText,
                                totalPages = structuredText.totalPages
                            )
                        } else it
                    }
                }.onFailure { error ->
                    android.util.Log.e("LibraryScreen", "Ошибка структурирования", error)
                }

                isProcessing = false
                processingText = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Библиотека учебников") },
                actions = {
                    IconButton(onClick = { pdfPicker.launch("application/pdf") }) {
                        Icon(Icons.Rounded.Add, "Добавить PDF")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pdfPicker.launch("application/pdf") }) {
                Icon(Icons.Rounded.FileUpload, "Загрузить PDF")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isProcessing) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(Modifier.size(24.dp))
                            Spacer(Modifier.width(16.dp))
                            Text(processingText)
                        }
                    }
                }
            }

            if (textbooks.isEmpty() && !isProcessing) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Icon(Icons.Rounded.Book, null, Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Ваши учебники появятся здесь", style = MaterialTheme.typography.titleMedium)
                            Text("Загрузите PDF файлы учебников для удобного чтения и структурирования через AI", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                items(textbooks) { item ->
                    TextbookItemCard(
                        item = item,
                        onStructureClick = { structureTextbook(item) },
                        onViewStructure = { selectedItem = item }
                    )
                }
            }
        }
    }

    // Диалог просмотра структуры
    selectedItem?.let { item ->
        item.structuredText?.let { structuredText ->
            AlertDialog(
                onDismissRequest = { selectedItem = null },
                title = { Text(item.title) },
                text = {
                    LazyColumn {
                        item {
                            Text("Резюме: ${structuredText.summary}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(16.dp))
                        }

                        items(structuredText.paragraphs) { para ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(para.title, style = MaterialTheme.typography.titleSmall)
                                        Text("Стр. ${para.pageNumber}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(para.content, style = MaterialTheme.typography.bodySmall)
                                    Text("Важность: ${para.importance}/5", style = MaterialTheme.typography.bodySmall)
                                    Text("Ключевые слова: ${para.keywords.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedItem = null }) {
                        Text("Закрыть")
                    }
                }
            )
        }
    }
}
