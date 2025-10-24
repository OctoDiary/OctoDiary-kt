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
fun AnalyticsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аналитика") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Карта знаний
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AccountTree, null, Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Карта знаний", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Визуализация освоенных тем", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Прогноз оценок
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Rounded.TrendingUp, null, Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Прогноз оценок", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("AI предсказывает ваши будущие оценки", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Статистика ошибок
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, null, Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Типичные ошибки", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Анализ частых ошибок по предметам", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
