package org.bxkr.octodiary.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.components.TocViewer
import org.bxkr.octodiary.managers.TextbookManager
import org.bxkr.octodiary.models.Textbook
import org.bxkr.octodiary.models.TocEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экран управления учебниками
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextbooksScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var textbooks by remember { mutableStateOf(TextbookManager.getAllTextbooks(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTextbook by remember { mutableStateOf<Textbook?>(null) }
    var showTocDialog by remember { mutableStateOf(false) }
    var selectedTocTextbook by remember { mutableStateOf<Textbook?>(null) }
    var isGeneratingToc by remember { mutableStateOf(false) }

    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            showAddDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Учебники") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                        Icon(Icons.Rounded.Add, "Добавить учебник")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { pdfPickerLauncher.launch("application/pdf") },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Добавить PDF") }
            )
        }
    ) { padding ->
        if (textbooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Нет учебников",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Добавьте PDF учебник, чтобы всегда иметь его под рукой",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(textbooks) { textbook ->
                    TextbookCard(
                        textbook = textbook,
                        onClick = {
                            // Открыть PDF
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    Uri.parse(textbook.filePath),
                                    "application/pdf"
                                )
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                        },
                        onViewToc = {
                            selectedTocTextbook = textbook
                            showTocDialog = true
                        },
                        onDelete = {
                            TextbookManager.deleteTextbook(context, textbook.id)
                            textbooks = TextbookManager.getAllTextbooks(context)
                        }
                    )
                }
            }
        }
    }
    
    // Диалог добавления учебника
    if (showAddDialog && selectedPdfUri != null) {
        AddTextbookDialog(
            onDismiss = { 
                showAddDialog = false
                selectedPdfUri = null
            },
            onAdd = { subject, title, author ->
                scope.launch {
                    val result = TextbookManager.addTextbook(
                        context = context,
                        uri = selectedPdfUri!!,
                        subjectName = subject,
                        title = title,
                        author = author
                    )
                    result.onSuccess {
                        textbooks = TextbookManager.getAllTextbooks(context)
                        android.widget.Toast.makeText(
                            context,
                            "Учебник добавлен",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure { error ->
                        android.widget.Toast.makeText(
                            context,
                            "Ошибка: ${error.message}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    showAddDialog = false
                    selectedPdfUri = null
                }
            }
        )
    }

    // Диалог оглавления
    if (showTocDialog && selectedTocTextbook != null) {
        AlertDialog(
            onDismissRequest = { showTocDialog = false },
            title = { Text("Оглавление: ${selectedTocTextbook?.title}") },
            text = {
                Box(modifier = Modifier.height(400.dp)) {
                    TocViewer(
                        textbookId = selectedTocTextbook!!.id,
                        onGenerateToc = { onProgress ->
                            isGeneratingToc = true
                            scope.launch {
                                try {
                                    // TODO: Реализовать генерацию с прогрессом
                                    val result = TextbookManager.generateTocForTextbook(context, selectedTocTextbook!!)
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
                                } finally {
                                    isGeneratingToc = false
                                }
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTocDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Показываем прогресс генерации
    if (isGeneratingToc) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Создание оглавления") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("AI анализирует учебник...")
                }
            },
            confirmButton = { }
        )
    }
}

@Composable
private fun TextbookCard(
    textbook: Textbook,
    onClick: () -> Unit,
    onViewToc: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Rounded.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    textbook.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    textbook.subjectName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (textbook.author != null) {
                    Text(
                        "Автор: ${textbook.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    "${formatFileSize(textbook.fileSize)} • ${formatDate(textbook.addedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onViewToc) {
                    Icon(Icons.Rounded.List, "Оглавление")
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Rounded.Delete, "Удалить")
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить учебник?") },
            text = { Text("Файл \"${textbook.title}\" будет удалён из приложения") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTextbookDialog(
    onDismiss: () -> Unit,
    onAdd: (subject: String, title: String, author: String?) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    
    var subjectExpanded by remember { mutableStateOf(false) }
    
    val subjects = listOf(
        "Математика", "Русский язык", "Английский язык",
        "Физика", "Химия", "Биология", "География",
        "История", "Обществознание", "Литература",
        "Информатика", "Другое"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить учебник") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Предмет
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Предмет") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj) },
                                onClick = {
                                    subject = subj
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Название
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название учебника") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Автор (опционально)
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Автор (необязательно)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && title.isNotBlank()) {
                        onAdd(subject, title, author.ifBlank { null })
                    }
                },
                enabled = subject.isNotBlank() && title.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes Б"
        bytes < 1024 * 1024 -> "${bytes / 1024} КБ"
        else -> "${bytes / (1024 * 1024)} МБ"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
