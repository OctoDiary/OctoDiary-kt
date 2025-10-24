package org.bxkr.octodiary.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сравнение со средними") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ваш средний балл", style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                            Text("4.5", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("средний по классу: 4.2", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = 0.75f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Вы в топ 25% класса", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("По предметам:", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        SubjectComparison("Математика", 4.8f, 4.1f)
                        SubjectComparison("Физика", 4.5f, 4.3f)
                        SubjectComparison("Английский", 4.2f, 4.0f)
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectComparison(subject: String, yourGrade: Float, avgGrade: Float) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(subject, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("Вы: $yourGrade", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(8.dp))
        Text("Класс: $avgGrade", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
