package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Конспекты") },
                actions = {
                    IconButton(onClick = { /* OCR */ }) {
                        Icon(Icons.Default.CameraAlt, "Сканировать")
                    }
                    IconButton(onClick = { /* Voice */ }) {
                        Icon(Icons.Default.Mic, "Голосовой ввод")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, "Новый конспект")
            }
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
                        Icon(Icons.Default.NoteAdd, null, Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Создайте первый конспект", style = MaterialTheme.typography.titleMedium)
                        Text("Сфотографируйте доску или введите текст", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}



