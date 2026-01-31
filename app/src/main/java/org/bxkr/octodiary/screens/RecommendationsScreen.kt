package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Умные рекомендации") }
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
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Lightbulb, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Начните делать ДЗ в 18:00", style = MaterialTheme.typography.titleMedium)
                        Text("С учётом времени отхода ко сну (21:00) и сложности заданий", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Рекомендуемый порядок выполнения ДЗ:", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("1. Математика (~45 мин)", style = MaterialTheme.typography.bodySmall)
                        Text("2. Физика (~30 мин)", style = MaterialTheme.typography.bodySmall)
                        Text("3. Английский (~20 мин)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}



