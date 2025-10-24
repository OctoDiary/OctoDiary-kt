package org.bxkr.octodiary.components.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.GeminiService

/**
 * JSON структура расширенного теста
 */
data class AdvancedQuizData(
    @SerializedName("title") val title: String? = null,
    @SerializedName("questions") val questions: List<QuizQuestion>? = null
)

data class QuizQuestion(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: QuestionType,
    @SerializedName("text") val text: String? = null,
    @SerializedName("options") val options: List<String>? = null,
    @SerializedName("correctAnswer") val correctAnswer: String? = null,
    @SerializedName("pairs") val pairs: List<Pair<String, String>>? = null
)

enum class QuestionType {
    @SerializedName("single_choice") SINGLE_CHOICE,      // Выбор из вариантов
    @SerializedName("text_input") TEXT_INPUT,            // Ввод текста
    @SerializedName("matching") MATCHING                 // Соединение пар
}

/**
 * Парсит расширенный тест из JSON
 */
fun parseAdvancedQuizFromJson(text: String): AdvancedQuizData? {
    return try {
        // Ищем JSON блок в тексте
        val jsonStart = text.indexOf("{")
        val jsonEnd = text.lastIndexOf("}") + 1
        
        if (jsonStart == -1 || jsonEnd <= jsonStart) return null
        
        val jsonText = text.substring(jsonStart, jsonEnd)
        Gson().fromJson(jsonText, AdvancedQuizData::class.java)
    } catch (e: Exception) {
        android.util.Log.e("AdvancedQuiz", "Ошибка парсинга JSON: ${e.message}")
        null
    }
}

/**
 * Расширенный интерактивный тест
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedQuiz(
    quizData: AdvancedQuizData,
    context: Context,
    modifier: Modifier = Modifier
) {
    var userAnswers by remember { mutableStateOf(mutableMapOf<Int, Any>()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var aiReview by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                quizData.title ?: "Тест",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Вопросы
            val questions = quizData.questions ?: emptyList()
            if (questions.isEmpty()) {
                Text(
                    "Ошибка: тест не содержит вопросов",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                questions.forEach { question ->
                when (question.type) {
                    QuestionType.SINGLE_CHOICE -> {
                        SingleChoiceQuestion(
                            question = question,
                            selectedAnswer = userAnswers[question.id] as? String,
                            onAnswerSelected = { answer ->
                                userAnswers[question.id] = answer
                            },
                            showCorrect = false
                        )
                    }
                    QuestionType.TEXT_INPUT -> {
                        TextInputQuestion(
                            question = question,
                            answer = userAnswers[question.id] as? String ?: "",
                            onAnswerChanged = { answer ->
                                userAnswers[question.id] = answer
                            }
                        )
                    }
                    QuestionType.MATCHING -> {
                        MatchingQuestion(
                            question = question,
                            matches = userAnswers[question.id] as? Map<String, String> ?: emptyMap(),
                            onMatchesChanged = { matches ->
                                userAnswers[question.id] = matches
                            }
                        )
                    }
                }
                }
            }
            
            // Кнопки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            
                            // Отправляем ответы AI на проверку
                            val reviewPrompt = buildReviewPrompt(quizData, userAnswers)
                            val result = GeminiService.sendMessage(
                                context = context,
                                prompt = reviewPrompt
                            )
                            
                            result.onSuccess { review ->
                                aiReview = review
                            }.onFailure { error ->
                                aiReview = "Ошибка проверки: ${error.message}"
                            }
                            
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting && userAnswers.size == (quizData.questions?.size ?: 0)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isSubmitting) "Проверяю..." else "Отправить на проверку")
                }
            }
            
            // Отзыв от AI
            AnimatedVisibility(visible = aiReview != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "📝 Проверка AI",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            aiReview ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Вопрос с выбором из вариантов
 */
@Composable
private fun SingleChoiceQuestion(
    question: QuizQuestion,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
    showCorrect: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Вопрос ${question.id}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                question.text ?: "[Вопрос без текста]",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(4.dp))
            
            question.options?.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == option
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onAnswerSelected(option) }
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Text(
                        text = buildString {
                            append(('А' + index).toString())
                            append(") ")
                            append(option)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Вопрос с вводом текста
 */
@Composable
private fun TextInputQuestion(
    question: QuizQuestion,
    answer: String,
    onAnswerChanged: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Вопрос ${question.id}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                question.text ?: "[Вопрос без текста]",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(4.dp))
            
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Введите ответ...") },
                singleLine = false,
                maxLines = 3
            )
        }
    }
}

/**
 * Вопрос на соединение пар
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchingQuestion(
    question: QuizQuestion,
    matches: Map<String, String>,
    onMatchesChanged: (Map<String, String>) -> Unit
) {
    val pairs = question.pairs ?: emptyList()
    if (pairs.isEmpty()) {
        Text("Ошибка: нет пар для сопоставления", color = MaterialTheme.colorScheme.error)
        return
    }
    val leftItems = pairs.map { it.first }
    val rightItems = pairs.map { it.second }.shuffled()
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Вопрос ${question.id}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                question.text ?: "[Вопрос без текста]",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Упрощённый UI - выпадающие списки для каждой пары
            leftItems.forEach { leftItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        leftItem,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    val selectedRight = matches[leftItem]
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedRight ?: "Выберите...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            rightItems.forEach { rightItem ->
                                DropdownMenuItem(
                                    text = { Text(rightItem) },
                                    onClick = {
                                        val newMatches = matches.toMutableMap()
                                        newMatches[leftItem] = rightItem
                                        onMatchesChanged(newMatches)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Построить промпт для проверки ответов
 */
private fun buildReviewPrompt(quizData: AdvancedQuizData, userAnswers: Map<Int, Any>): String {
    return buildString {
        appendLine("Проверь ответы ученика на тест и дай подробную обратную связь.")
        appendLine()
        appendLine("**Тест**: ${quizData.title}")
        appendLine()
        
        quizData.questions?.forEach { question ->
            val questionText = question.text ?: "[Вопрос без текста]"
            appendLine("**Вопрос ${question.id}**: $questionText")
            
            when (question.type) {
                QuestionType.SINGLE_CHOICE -> {
                    appendLine("Правильный ответ: ${question.correctAnswer}")
                    appendLine("Ответ ученика: ${userAnswers[question.id] ?: "не дан"}")
                }
                QuestionType.TEXT_INPUT -> {
                    appendLine("Ожидаемый ответ: ${question.correctAnswer}")
                    appendLine("Ответ ученика: ${userAnswers[question.id] ?: "не дан"}")
                }
                QuestionType.MATCHING -> {
                    appendLine("Правильные пары: ${question.pairs}")
                    appendLine("Ответ ученика: ${userAnswers[question.id]}")
                }
            }
            appendLine()
        }
        
        appendLine()
        appendLine("**Твоя задача:**")
        appendLine("1. Проверь каждый ответ")
        appendLine("2. Укажи что правильно, что неправильно")
        appendLine("3. Объясни ошибки")
        appendLine("4. Дай рекомендации для улучшения")
        appendLine("5. Если есть ошибки - предложи создать похожий тест для пересдачи")
        appendLine()
        appendLine("Будь дружелюбным и мотивирующим!")
    }
}
