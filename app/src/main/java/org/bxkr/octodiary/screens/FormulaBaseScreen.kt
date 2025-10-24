package org.bxkr.octodiary.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Formula(
    val id: Int,
    val subject: String,
    val category: String,
    val name: String,
    val formula: String,
    val description: String,
    val example: String? = null
)

val defaultFormulas = listOf(
    Formula(1, "Математика", "Алгебра", "Квадратное уравнение", "ax² + bx + c = 0\nD = b² - 4ac\nx = (-b ± √D) / 2a", "Решение квадратного уравнения через дискриминант", "x² - 5x + 6 = 0"),
    Formula(2, "Физика", "Механика", "Второй закон Ньютона", "F = ma", "Сила равна произведению массы на ускорение", "F = 10кг × 2м/с² = 20Н"),
    Formula(3, "Математика", "Геометрия", "Теорема Пифагора", "a² + b² = c²", "В прямоугольном треугольнике квадрат гипотенузы равен сумме квадратов катетов", "3² + 4² = 5²"),
    Formula(4, "Физика", "Кинематика", "Путь при равноускоренном движении", "S = v₀t + (at²)/2", "Путь при движении с постоянным ускорением", null),
    Formula(5, "Химия", "Общая химия", "Формула массы вещества", "m = n × M", "Масса равна количеству вещества умноженному на молярную массу", "m = 2моль × 18г/моль = 36г")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaBaseScreen() {
    var selectedSubject by remember { mutableStateOf("Все") }
    val subjects = listOf("Все", "Математика", "Физика", "Химия", "Биология")
    
    val filteredFormulas = if (selectedSubject == "Все") {
        defaultFormulas
    } else {
        defaultFormulas.filter { it.subject == selectedSubject }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("База формул") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Rounded.Add, "Добавить")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Фильтр по предметам
            ScrollableTabRow(selectedTabIndex = subjects.indexOf(selectedSubject)) {
                subjects.forEach { subject ->
                    Tab(
                        selected = selectedSubject == subject,
                        onClick = { selectedSubject = subject },
                        text = { Text(subject) }
                    )
                }
            }
            
            LazyColumn(Modifier.fillMaxSize()) {
                items(filteredFormulas) { formula ->
                    FormulaCard(formula)
                }
            }
        }
    }
}

@Composable
fun FormulaCard(formula: Formula) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formula.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        formula.category,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    formula.formula,
                    Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                formula.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            formula.example?.let { example ->
                Spacer(Modifier.height(8.dp))
                Row {
                    Icon(
                        Icons.Rounded.Calculate,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Пример: $example",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
