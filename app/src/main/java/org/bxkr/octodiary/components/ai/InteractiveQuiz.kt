package org.bxkr.octodiary.components.ai


import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Парсит тест из текста AI
 */
fun parseQuizFromText(text: String): QuizData? {
    // Определяем, содержит ли текст тест
    val hasQuizMarkers = text.contains("**Вопрос", ignoreCase = true) ||
            text.contains("### Вопрос", ignoreCase = true) ||
            (text.contains("Вопрос") && (text.contains("a)", ignoreCase = true) || text.contains("А)", ignoreCase = true)))
    
    if (!hasQuizMarkers) return null
    
    val questions = mutableListOf<Question>()
    
    // Разбиваем на строки и ищем вопросы
    val lines = text.lines()
    var currentQuestion: String? = null
    val currentOptions = mutableListOf<String>()
    var correctAnswer: String? = null
    
    for (line in lines) {
        val trimmed = line.trim()
        
        // Новый вопрос (разные форматы)
        val isQuestion = trimmed.matches(Regex("^(\\*\\*)?###?\\s*Вопрос\\s*\\d+.*", setOf(RegexOption.IGNORE_CASE))) ||
            trimmed.matches(Regex("^Вопрос\\s*\\d+[\\.:)].*", setOf(RegexOption.IGNORE_CASE))) ||
            trimmed.matches(Regex("^\\d+[\\.\\)]\\s+.*", setOf(RegexOption.IGNORE_CASE)))
        
        if (isQuestion) {
            
            // Сохраняем предыдущий вопрос
            if (currentQuestion != null && currentOptions.isNotEmpty()) {
                questions.add(Question(
                    text = currentQuestion,
                    options = currentOptions.toList(),
                    correctAnswer = correctAnswer
                ))
            }
            
            currentQuestion = trimmed
                .replace(Regex("^\\*\\*###?\\s*Вопрос\\s*\\d+[\\.:)]?\\s*", setOf(RegexOption.IGNORE_CASE)), "")
                .replace(Regex("^Вопрос\\s*\\d+[\\.:)]?\\s*", setOf(RegexOption.IGNORE_CASE)), "")
                .replace(Regex("^\\d+[\\.\\)]\\s*"), "")
                .replace("**", "")
                .trim()
            currentOptions.clear()
            correctAnswer = null
        }
        // Варианты ответа (a), b), А), Б) и т.д.)
        else if (trimmed.matches(Regex("^[а-яa-z][\\.\\)].*", setOf(RegexOption.IGNORE_CASE)))) {
            val option = trimmed.substring(2).trim()
            currentOptions.add(option)
        }
        // Правильный ответ
        else if (trimmed.matches(Regex("^(Ответ|Правильный|Correct).*:", setOf(RegexOption.IGNORE_CASE)))) {
            correctAnswer = trimmed
                .substringAfter(":")
                .trim()
                .replace(Regex("[а-яa-z]\\)", setOf(RegexOption.IGNORE_CASE)), "")
                .trim()
        }
    }
    
    // Сохраняем последний вопрос
    if (currentQuestion != null && currentOptions.isNotEmpty()) {
        questions.add(Question(
            text = currentQuestion,
            options = currentOptions.toList(),
            correctAnswer = correctAnswer
        ))
    }
    
    return if (questions.isNotEmpty()) {
        QuizData(questions = questions)
    } else null
}

data class QuizData(
    val questions: List<Question>
)

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswer: String? = null
)

/**
 * Интерактивный тест
 */
@Composable
fun InteractiveQuiz(
    quizData: QuizData,
    modifier: Modifier = Modifier
) {
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, String>()) }
    var showResults by remember { mutableStateOf(false) }
    
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
                "📝 Тест (${quizData.questions.size} вопросов)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            quizData.questions.forEachIndexed { index, question ->
                QuestionCard(
                    questionNumber = index + 1,
                    question = question,
                    selectedAnswer = selectedAnswers[index],
                    showResults = showResults,
                    onAnswerSelected = { answer ->
                        selectedAnswers[index] = answer
                    }
                )
            }
            
            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!showResults) {
                    Button(
                        onClick = {
                            if (selectedAnswers.size == quizData.questions.size) {
                                showResults = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedAnswers.size == quizData.questions.size
                    ) {
                        Text("Проверить ответы")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            selectedAnswers.clear()
                            showResults = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Начать заново")
                    }
                }
            }
            
            // Результаты
            if (showResults && quizData.questions.any { it.correctAnswer != null }) {
                val correctCount = quizData.questions.filterIndexed { index, question ->
                    question.correctAnswer != null && 
                    selectedAnswers[index]?.contains(question.correctAnswer, ignoreCase = true) == true
                }.size
                
                val totalWithAnswers = quizData.questions.count { it.correctAnswer != null }
                
                if (totalWithAnswers > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (correctCount == totalWithAnswers) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    ) {
                        Text(
                            "Результат: $correctCount из $totalWithAnswers правильных ответов",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    questionNumber: Int,
    question: Question,
    selectedAnswer: String?,
    showResults: Boolean,
    onAnswerSelected: (String) -> Unit
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
                "Вопрос $questionNumber",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                question.text,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(Modifier.height(4.dp))
            
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == option
                val isCorrect = showResults && question.correctAnswer != null && 
                        option.contains(question.correctAnswer, ignoreCase = true)
                val isWrong = showResults && isSelected && !isCorrect
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { if (!showResults) onAnswerSelected(option) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { if (!showResults) onAnswerSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = when {
                                isCorrect -> MaterialTheme.colorScheme.primary
                                isWrong -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Text(
                        text = buildString {
                            append(('А' + index).toString())
                            append(") ")
                            append(option)
                            
                            if (showResults && isCorrect) {
                                append(" ✓")
                            } else if (showResults && isWrong) {
                                append(" ✗")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isCorrect -> MaterialTheme.colorScheme.primary
                            isWrong -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}



