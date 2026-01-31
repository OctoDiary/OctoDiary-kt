package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.GeminiService
// import org.bxkr.octodiary.database.AppDatabase
// import org.bxkr.octodiary.database.entity.VocabularyEntity
import org.bxkr.octodiary.navControllerLive

/**
 * Умный словарь с OCR
 * Фоткаешь таблицу слов → они добавляются в приложение
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularySmartScreen() {
    val context = LocalContext.current
    val nav = navControllerLive.value
    val scope = rememberCoroutineScope()
    
    var words by remember { mutableStateOf<List<Any>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("en") }
    var selectedSubject by remember { mutableStateOf("Английский язык") }
    
    // Загрузка слов
    // LaunchedEffect(selectedLanguage) { // Placeholder
    //     val db = AppDatabase.getDatabase(context)
    //     words = db?.vocabularyDao()?.getWordsByLanguage(selectedLanguage) ?: emptyList()
    // }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        
        isLoading = true
        scope.launch {
            try {
                // 1. OCR через ML Kit
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val fullText = visionText.text
                        
                        // 2. Отправляем в Gemini для структурирования
                        scope.launch {
                            val prompt = """
                                Ты получил текст с фотографии словаря или списка слов.
                                Извлеки из него пары "слово - перевод" и верни в формате JSON:
                                [
                                  {"word": "apple", "translation": "яблоко"},
                                  {"word": "book", "translation": "книга"}
                                ]
                                
                                Текст:
                                $fullText
                            """.trimIndent()
                            
                            isLoading = true
                            val result = GeminiService.sendMessage(context, prompt)
                            isLoading = false
                            
                            result.onSuccess { response ->
                                // Парсим JSON и добавляем слова
                                try {
                                    // val jsonArray = org.json.JSONArray(response.substringAfter("[").substringBeforeLast("]").let { "[$it]" }) // Placeholder
                                    // val db = AppDatabase.getDatabase(context) ?: return@launch
                                    // val newWords = mutableListOf<VocabularyEntity>()
                                    //
                                    // for (i in 0 until jsonArray.length()) {
                                    //     val obj = jsonArray.getJSONObject(i)
                                    //     newWords.add(
                                    //         VocabularyEntity(
                                    //             word = obj.getString("word"),
                                    //             translation = obj.getString("translation"),
                                    //             language = selectedLanguage,
                                    //             subjectName = selectedSubject,
                                    //             createdAt = System.currentTimeMillis()
                                    //         )
                                    //     )
                                    // }
                                    //
                                    // db.vocabularyDao().insertWords(newWords)
                                    // words = db.vocabularyDao().getWordsByLanguage(selectedLanguage)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            isLoading = false
                        }
                    }
                    .addOnFailureListener {
                        isLoading = false
                    }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Умный словарь") },
                navigationIcon = {
                    IconButton(onClick = { nav?.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.CameraAlt, "Сканировать")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Добавить слово вручную */ }) {
                Icon(Icons.Default.Add, "Добавить")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Фильтры
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en"; selectedSubject = "Английский язык" },
                    label = { Text("English") }
                )
                FilterChip(
                    selected = selectedLanguage == "de",
                    onClick = { selectedLanguage = "de"; selectedSubject = "Немецкий язык" },
                    label = { Text("Deutsch") }
                )
                FilterChip(
                    selected = selectedLanguage == "fr",
                    onClick = { selectedLanguage = "fr"; selectedSubject = "Французский язык" },
                    label = { Text("Français") }
                )
            }
            
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Распознаю слова...")
                    }
                }
            } else if (words.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            null,
                            Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Нет слов", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Сфотографируйте таблицу слов из тетради",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(words) { word: Any ->
                        VocabularyCard(word, onDelete = {
                            // Placeholder for onDelete
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun VocabularyCard(word: Any, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Слово", // Placeholder
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Перевод", // Placeholder
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Удалить")
            }
        }
    }
}



