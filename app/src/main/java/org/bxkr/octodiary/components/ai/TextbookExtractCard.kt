package org.bxkr.octodiary.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.database.entity.TextbookExtractEntity
import org.bxkr.octodiary.ai.TextbookExtractorService

/**
 * Карточка извлечённого элемента из учебника (вопрос, упражнение, задание)
 */
@Composable
fun TextbookExtractCard(
    extract: TextbookExtractEntity,
    onCompletionChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isCompleted by remember { mutableStateOf(extract.isCompleted) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Заголовок с типом и чекбоксом
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Иконка типа
                    val (icon, color) = when (extract.extractType) {
                        "question" -> Icons.Rounded.QuestionMark to MaterialTheme.colorScheme.primary
                        "exercise" -> Icons.Rounded.Edit to MaterialTheme.colorScheme.secondary
                        "assignment" -> Icons.AutoMirrored.Rounded.Assignment to MaterialTheme.colorScheme.tertiary
                        else -> Icons.AutoMirrored.Rounded.Help to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = when (extract.extractType) {
                            "question" -> "Вопрос"
                            "exercise" -> "Упражнение"
                            "assignment" -> "Задание"
                            else -> "Элемент"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = color
                    )
                }

                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = {
                        isCompleted = it
                        onCompletionChanged(it)
                    }
                )
            }

            // Основной контент
            Text(
                text = extract.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Контекст (если есть)
            if (extract.context.isNotBlank()) {
                Text(
                    text = extract.context,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Метаданные (сложность и время)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                extract.difficulty?.let { difficulty ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Сложность: $difficulty/5",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                extract.estimatedTime?.let { time ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$time мин",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка с полем ввода текста для извлечения элементов
 */
@Composable
fun TextbookExtractInputCard(
    onExtract: (String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Извлечь вопросы, упражнения и задания",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Текст из учебника") },
                placeholder = { Text("Вставьте текст параграфа из учебника...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines = 4,
                maxLines = 10
            )

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onExtract(text)
                    }
                },
                enabled = text.isNotBlank() && !isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Извлечение...")
                } else {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Извлечь")
                }
            }
        }
    }
}