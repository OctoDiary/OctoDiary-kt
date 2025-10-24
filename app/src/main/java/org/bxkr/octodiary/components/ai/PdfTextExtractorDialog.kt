package org.bxkr.octodiary.components.ai

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.PdfTextExtractor

/**
 * Диалог для извлечения текста из PDF файлов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfTextExtractorDialog(
    onDismiss: () -> Unit,
    onTextExtracted: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var isExtracting by remember { mutableStateOf(false) }
    var extractedText by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Выборщик файлов
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedUri = it
            fileName = getFileNameFromUri(context, it)
            extractedText = null
            error = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Извлечение текста из PDF") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Выбор файла
                if (selectedUri == null) {
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/pdf"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.FileOpen, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Выбрать PDF файл")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Description,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    fileName ?: "PDF файл",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "PDF документ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedUri = null
                                    fileName = null
                                    extractedText = null
                                    error = null
                                }
                            ) {
                                Icon(Icons.Rounded.Clear, null)
                            }
                        }
                    }

                    // Кнопка извлечения
                    Button(
                        onClick = {
                            selectedUri?.let { uri ->
                                isExtracting = true
                                error = null
                                scope.launch {
                                    val result = PdfTextExtractor.extractText(context, uri)
                                    result.onSuccess { text ->
                                        extractedText = text
                                    }.onFailure { e ->
                                        error = e.message ?: "Ошибка при извлечении текста"
                                    }
                                    isExtracting = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExtracting
                    ) {
                        if (isExtracting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Rounded.TextSnippet, null)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (isExtracting) "Извлечение..." else "Извлечь текст")
                    }
                }

                // Ошибка
                error?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Error,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Извлеченный текст
                extractedText?.let { text ->
                    Text(
                        "Извлеченный текст:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (extractedText != null) {
                Button(
                    onClick = {
                        extractedText?.let { onTextExtracted(it) }
                        onDismiss()
                    }
                ) {
                    Text("Использовать текст")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

/**
 * Получить имя файла из URI
 */
private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    it.getString(displayNameIndex)
                } else null
            } else null
        }
    } catch (e: Exception) {
        null
    }
}