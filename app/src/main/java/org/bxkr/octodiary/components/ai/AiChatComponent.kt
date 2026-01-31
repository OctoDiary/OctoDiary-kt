package org.bxkr.octodiary.components.ai



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.ai.ChatMessage
import org.bxkr.octodiary.ai.GeminiService
import org.bxkr.octodiary.components.ai.parseQuizFromText
import org.bxkr.octodiary.components.ai.InteractiveQuiz
import java.io.InputStream

/**
 * Компонент чата с AI
 */
@Composable
fun AiChatComponent(
    chatId: String,
    systemPrompt: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Загружаем историю чата при запуске
    LaunchedEffect(chatId) {
        val prefs = context.getSharedPreferences("ai_chats", android.content.Context.MODE_PRIVATE)
        val savedMessages = prefs.getString("chat_$chatId", null)
        if (savedMessages != null) {
            try {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<ChatMessage>>() {}.type
                messages = gson.fromJson(savedMessages, type)
            } catch (e: Exception) {
                android.util.Log.e("AiChat", "Ошибка загрузки чата: ${e.message}")
            }
        }
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    Column(modifier = modifier) {
        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(
                    message = message,
                    useMarkdown = !message.isUser
                )
            }
            
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Думаю...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Icon(Icons.Default.AttachFile, "Прикрепить")
            }
            
            // Кнопка очистки чата
            IconButton(
                onClick = {
                    messages = emptyList()
                    saveChatHistory(context, chatId, emptyList())
                    android.widget.Toast.makeText(
                        context,
                        "Чат очищен",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                enabled = messages.isNotEmpty()
            ) {
                Icon(Icons.Default.Delete, "Очистить чат")
            }
            
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Спросите что-нибудь...") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                maxLines = 4
            )
            
            FilledIconButton(
                onClick = {
                    if (inputText.isBlank() && selectedImageUri == null) return@FilledIconButton
                    
                    val message = inputText
                    val imageUri = selectedImageUri
                    
                    inputText = ""
                    selectedImageUri = null
                    isLoading = true
                    
                    scope.launch {
                        // Добавляем сообщение пользователя
                        messages = messages + ChatMessage(
                            content = message,
                            isUser = true,
                            imageUri = imageUri?.toString()
                        )
                        
                        // Скроллим вниз
                        listState.animateScrollToItem(messages.size)
                        
                        // Отправляем запрос к AI
                        val result = if (imageUri != null) {
                            // С изображением
                            val bitmap = loadBitmapFromUri(context.contentResolver.openInputStream(imageUri))
                            if (bitmap != null) {
                                GeminiService.sendImageMessage(context, message, bitmap, systemPrompt)
                            } else {
                                Result.failure(Exception("Не удалось загрузить изображение"))
                            }
                        } else {
                            // Только текст
                            GeminiService.sendMessage(
                                context = context,
                                prompt = if (systemPrompt.isNotEmpty()) "$systemPrompt\n\n$message" else message,
                                history = messages.map { 
                                    ChatMessage(it.content, it.isUser)
                                }
                            )
                        }
                        
                        isLoading = false
                        
                        result.onSuccess { response ->
                            messages = messages + ChatMessage(
                                content = response,
                                isUser = false
                            )
                            // Сохраняем историю чата
                            saveChatHistory(context, chatId, messages)
                            listState.animateScrollToItem(messages.size)
                        }.onFailure { error ->
                            messages = messages + ChatMessage(
                                content = "❌ Ошибка: ${error.message}",
                                isUser = false
                            )
                            // Сохраняем и ошибки тоже
                            saveChatHistory(context, chatId, messages)
                        }
                    }
                },
                enabled = !isLoading && (inputText.isNotBlank() || selectedImageUri != null)
            ) {
                Icon(Icons.Default.Send, "Отправить")
            }
        }
        
        // Preview selected image
        selectedImageUri?.let { uri ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    "Прикреплено изображение",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    useMarkdown: Boolean = false
) {
    val context = LocalContext.current
    
    // Проверяем, есть ли в сообщении расширенный JSON тест
    val advancedQuizData = if (!message.isUser) {
        remember(message.content) {
            parseAdvancedQuizFromJson(message.content)
        }
    } else null
    
    // Проверяем, есть ли в сообщении обычный тест
    val quizData = if (!message.isUser && advancedQuizData == null) {
        remember(message.content) {
            parseQuizFromText(message.content)
        }
    } else null
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (advancedQuizData != null) {
            // Расширенный интерактивный тест с AI проверкой
            AdvancedQuiz(
                quizData = advancedQuizData,
                context = context,
                modifier = Modifier.widthIn(max = 400.dp)
            )
        } else if (quizData != null) {
            // Обычный интерактивный тест
            InteractiveQuiz(
                quizData = quizData,
                modifier = Modifier.widthIn(max = 400.dp)
            )
        } else {
            // Обычное сообщение
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (message.isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                if (useMarkdown) {
                    MarkdownText(
                        text = message.content,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = message.content,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isUser) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

private fun loadBitmapFromUri(inputStream: InputStream?): Bitmap? {
    return try {
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        null
    } finally {
        inputStream?.close()
    }
}

/**
 * Сохранить историю чата в SharedPreferences
 */
private fun saveChatHistory(context: android.content.Context, chatId: String, messages: List<ChatMessage>) {
    val prefs = context.getSharedPreferences("ai_chats", android.content.Context.MODE_PRIVATE)
    val gson = com.google.gson.Gson()
    val json = gson.toJson(messages)
    prefs.edit().putString("chat_$chatId", json).apply()
}



