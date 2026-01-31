package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// import org.bxkr.octodiary.database.entity.LectureNoteEntity
import org.bxkr.octodiary.navControllerLive
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureNotesScreen() {
    val context = LocalContext.current
    val nav = navControllerLive.value
    
    var notes by remember { mutableStateOf<List<Any>>(emptyList()) }
    var showRecordDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Конспекты") },
                navigationIcon = {
                    IconButton(onClick = { nav?.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showRecordDialog = true }) {
                Icon(Icons.Default.Mic, "Записать")
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Mic, null, Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Нет конспектов", style = MaterialTheme.typography.titleLarge)
                    Text("Начните запись урока")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note: Any ->
                    LectureNoteCard(note)
                }
            }
        }
    }
    
    // Диалог записи
    if (showRecordDialog) {
        RecordNoteDialog(
            onDismiss = { showRecordDialog = false },
            onStartRecording = { subjectName ->
                showRecordDialog = false
                // TODO: Запуск записи через RecordingService
                android.widget.Toast.makeText(
                    context,
                    "Запись урока \"$subjectName\" начата",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordNoteDialog(
    onDismiss: () -> Unit,
    onStartRecording: (String) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val subjects = listOf(
        "Математика", "Русский язык", "Английский язык",
        "Физика", "Химия", "Биология", "География",
        "История", "Обществознание", "Литература",
        "Информатика", "Другое"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Mic, null) },
        title = { Text("Записать урок") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Выберите предмет для записи конспекта")
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Предмет") },
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
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject) },
                                onClick = {
                                    subjectName = subject
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartRecording(subjectName) },
                enabled = subjectName.isNotBlank()
            ) {
                Icon(Icons.Default.FiberManualRecord, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Начать запись")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun LectureNoteCard(note: Any) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Заголовок конспекта", style = MaterialTheme.typography.titleMedium)
            Text("Название предмета", style = MaterialTheme.typography.bodyMedium)
            Text(
                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date()),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}



