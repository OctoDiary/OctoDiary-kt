package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class VocabWord(
    val id: Int,
    val word: String,
    val translation: String,
    val transcription: String?,
    val subject: String,
    val example: String?,
    val imageUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen() {
    val words = remember {
        listOf(
            VocabWord(1, "Hello", "Привет", "[həˈloʊ]", "English", "Hello, how are you?"),
            VocabWord(2, "World", "Мир", "[wɜːrld]", "English", "Hello, World!")
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Словарь") },
                actions = {
                    IconButton(onClick = { /* OCR from photo */ }) {
                        Icon(Icons.Default.CameraAlt, "Сфотографировать таблицу")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FileDownload, "Экспорт")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, "Добавить слово")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(words) { word ->
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
                            Text(word.word, style = MaterialTheme.typography.titleLarge)
                            IconButton(onClick = { /* Play audio */ }) {
                                Icon(Icons.Default.VolumeUp, "Произношение")
                            }
                        }
                        word.transcription?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(word.translation, style = MaterialTheme.typography.bodyLarge)
                        word.example?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("Пример: $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}



