package org.bxkr.octodiary.components.ai


import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Структура теста от AI
 */
data class AiTest(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("questions")
    val questions: List<AiTestQuestion>
)

data class AiTestQuestion(
    @SerializedName("question")
    val question: String,
    @SerializedName("type")
    val type: String, // "single_choice", "multiple_choice", "text"
    @SerializedName("options")
    val options: List<String>?,
    @SerializedName("correct_answer")
    val correctAnswer: Any? // String или List<String>
)

/**
 * Компонент для отображения теста от AI
 */
@Composable
fun AiTestComponent(
    test: AiTest,
    onComplete: (score: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentQuestion by remember { mutableIntStateOf(0) }
    var userAnswers by remember { mutableStateOf(mutableMapOf<Int, Any>()) }
    var showResults by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.padding(16.dp)) {
        if (!showResults) {
            // Прогресс
            LinearProgressIndicator(
                progress = { (currentQuestion + 1).toFloat() / test.questions.size },
                modifier = Modifier.fillMaxWidth(),
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Заголовок теста
            Text(
                text = test.title,
                style = MaterialTheme.typography.headlineSmall
            )
            
            if (test.description != null) {
                Text(
                    text = test.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Текущий вопрос
            val question = test.questions[currentQuestion]
            
            Text(
                text = "Вопрос ${currentQuestion + 1} из ${test.questions.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Варианты ответа
            when (question.type) {
                "single_choice" -> {
                    SingleChoiceQuestion(
                        options = question.options ?: emptyList(),
                        selectedOption = userAnswers[currentQuestion] as? String,
                        onOptionSelected = { userAnswers[currentQuestion] = it }
                    )
                }
                "multiple_choice" -> {
                    MultipleChoiceQuestion(
                        options = question.options ?: emptyList(),
                        selectedOptions = (userAnswers[currentQuestion] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        onOptionsChanged = { userAnswers[currentQuestion] = it }
                    )
                }
                "text" -> {
                    var textAnswer by remember { mutableStateOf(userAnswers[currentQuestion] as? String ?: "") }
                    OutlinedTextField(
                        value = textAnswer,
                        onValueChange = {
                            textAnswer = it
                            userAnswers[currentQuestion] = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ваш ответ") }
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Кнопки навигации
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentQuestion > 0) {
                    OutlinedButton(onClick = { currentQuestion-- }) {
                        Text("Назад")
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                
                if (currentQuestion < test.questions.size - 1) {
                    Button(onClick = { currentQuestion++ }) {
                        Text("Далее")
                    }
                } else {
                    Button(
                        onClick = {
                            showResults = true
                            val score = calculateScore(test, userAnswers)
                            onComplete(score, test.questions.size)
                        }
                    ) {
                        Text("Завершить")
                    }
                }
            }
        } else {
            // Результаты
            TestResults(
                test = test,
                userAnswers = userAnswers
            )
        }
    }
}

@Composable
private fun SingleChoiceQuestion(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            Surface(
                onClick = { onOptionSelected(option) },
                shape = MaterialTheme.shapes.medium,
                color = if (option == selectedOption) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    RadioButton(
                        selected = option == selectedOption,
                        onClick = { onOptionSelected(option) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(option)
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceQuestion(
    options: List<String>,
    selectedOptions: List<String>,
    onOptionsChanged: (List<String>) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            val isSelected = option in selectedOptions
            Surface(
                onClick = {
                    val newSelection = if (isSelected) {
                        selectedOptions - option
                    } else {
                        selectedOptions + option
                    }
                    onOptionsChanged(newSelection)
                },
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(option)
                }
            }
        }
    }
}

@Composable
private fun TestResults(
    test: AiTest,
    userAnswers: Map<Int, Any>
) {
    val score = calculateScore(test, userAnswers)
    
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Результат",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "$score из ${test.questions.size}",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    "${(score.toFloat() / test.questions.size * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Показываем все вопросы с ответами
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(test.questions.size) { index ->
                val question = test.questions[index]
                val userAnswer = userAnswers[index]
                val isCorrect = checkAnswer(question, userAnswer)
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Вопрос ${index + 1}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(question.question)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ваш ответ: $userAnswer",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!isCorrect) {
                            Text(
                                "Правильный ответ: ${question.correctAnswer}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun calculateScore(test: AiTest, userAnswers: Map<Int, Any>): Int {
    return test.questions.indices.count { index ->
        checkAnswer(test.questions[index], userAnswers[index])
    }
}

private fun checkAnswer(question: AiTestQuestion, userAnswer: Any?): Boolean {
    return when (question.type) {
        "single_choice", "text" -> {
            userAnswer?.toString()?.trim()?.lowercase() == 
                question.correctAnswer?.toString()?.trim()?.lowercase()
        }
        "multiple_choice" -> {
            val userList = (userAnswer as? List<*>)?.map { it.toString().trim().lowercase() }?.toSet()
            val correctList = (question.correctAnswer as? List<*>)?.map { it.toString().trim().lowercase() }?.toSet()
            userList == correctList
        }
        else -> false
    }
}

/**
 * Парсинг теста из JSON ответа AI
 */
fun parseAiTest(jsonString: String): AiTest? {
    return try {
        Gson().fromJson(jsonString, AiTest::class.java)
    } catch (e: Exception) {
        null
    }
}



