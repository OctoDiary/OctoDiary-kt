package org.bxkr.octodiary.components.settings


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.ai.GeminiService
import org.bxkr.octodiary.components.SwitchPreference
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

/**
 * Настройки AI в главном меню настроек
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettings() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    
    var apiKey by remember { mutableStateOf(GeminiService.getApiKey(context)) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showBedTimeDialog by remember { mutableStateOf(false) }
    var showCustomApiUrlDialog by remember { mutableStateOf(false) }
    
    // Путь к локальной GGUF модели (объявляем ДО launcher)
    var localModelPath by remember {
        mutableStateOf(activity.mainPrefs.get<String>("ai_local_model_path") ?: "")
    }
    
    // Activity Result Launcher для выбора GGUF файла
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val path = it.toString()
            localModelPath = path
            activity.mainPrefs.save("ai_local_model_path" to path)
            android.widget.Toast.makeText(
                context, 
                "Модель выбрана: ${path.substringAfterLast("/")}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Автозапись лекций (по умолчанию ВЫКЛЮЧЕНА)
    val autoRecordLectures = remember {
        mutableStateOf(activity.mainPrefs.get<Boolean>("ai_auto_record_lectures") ?: false)
    }
    
    // Launcher для запроса разрешения микрофона
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение получено - включаем автозапись
            autoRecordLectures.value = true
            activity.mainPrefs.save("ai_auto_record_lectures" to true)
        } else {
            // Разрешение отклонено
            android.widget.Toast.makeText(
                context,
                "Для автозаписи уроков требуется разрешение микрофона",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Включение AI фич
    val aiEnabled = remember {
        mutableStateOf(activity.mainPrefs.get<Boolean>("ai_enabled") ?: true)
    }
    
    // Провайдер AI
    val selectedProvider = remember {
        mutableStateOf(activity.mainPrefs.get<String>("ai_provider") ?: "google")
    }
    
    // Кастомная модель
    val customModel = remember {
        mutableStateOf(activity.mainPrefs.get<String>("ai_custom_model") ?: "")
    }
    
    // Кастомный API URL
    val customApiUrl = remember {
        mutableStateOf(activity.mainPrefs.get<String>("ai_custom_api_url") ?: "")
    }
    
    // Время сна
    val bedTimeHour = remember {
        mutableIntStateOf(activity.mainPrefs.get<Int>("ai_bed_time_hour") ?: 21)
    }
    val bedTimeMinute = remember {
        mutableIntStateOf(activity.mainPrefs.get<Int>("ai_bed_time_minute") ?: 0)
    }
    
    // Dropdown для провайдера
    var providerExpanded by remember { mutableStateOf(false) }
    var showCustomModelDialog by remember { mutableStateOf(false) }
    
    val providers = listOf(
        "google" to "Google Gemini",
        "openai" to "OpenAI GPT",
        "custom" to "Кастомный провайдер",
        "local" to "Локальная модель (Ollama)"
    )
    
    // Дефолтные модели для провайдеров (актуальные версии 2025)
    val defaultModels = mapOf(
        "google" to "gemini-2.0-flash-exp",
        "openai" to "gpt-4-turbo",
        "custom" to "",
        "local" to ""
    )
    
    // Получаем текущую модель или дефолтную
    val currentModel = customModel.value.ifEmpty { 
        defaultModels[selectedProvider.value] ?: "gemini-1.5-flash-latest"
    }
    
    Column(Modifier.padding(vertical = 8.dp)) {
        // Заголовок
        Text(
            "Настройки AI",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Включение AI
        SwitchPreference(
            title = "AI помощник",
            description = "Умный чат, анализ ДЗ, персональный план учёбы",
            listenState = aiEnabled
        ) {
            aiEnabled.value = it
            activity.mainPrefs.save("ai_enabled" to it)
        }
        
        androidx.compose.animation.AnimatedVisibility(visible = aiEnabled.value) {
            Column {
                Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                // Выбор провайдера
                Text(
                    "Провайдер AI",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = providerExpanded,
                    onExpandedChange = { providerExpanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = providers.find { it.first == selectedProvider.value }?.second ?: "Не выбрано",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = providerExpanded,
                        onDismissRequest = { providerExpanded = false }
                    ) {
                        providers.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedProvider.value = key
                                    activity.mainPrefs.save("ai_provider" to key)
                                    
                                    // ИСПРАВЛЕНИЕ БАГА: Сохраняем дефолтную модель для провайдера
                                    val defaultModel = defaultModels[key] ?: ""
                                    if (customModel.value.isEmpty() && defaultModel.isNotEmpty()) {
                                        customModel.value = defaultModel
                                        activity.mainPrefs.save("ai_custom_model" to defaultModel)
                                        android.util.Log.d("AiSettings", "Установлена дефолтная модель для $key: $defaultModel")
                                    }
                                    
                                    providerExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Настройка локальной GGUF модели
                if (selectedProvider.value == "local") {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Локальная GGUF модель",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (localModelPath.isEmpty()) 
                                    "Модель не выбрана. Нажмите \"Выбрать\" для загрузки GGUF файла."
                                else 
                                    "Модель: ${localModelPath.substringAfterLast("/")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { 
                                    // Открываем file picker для выбора любых файлов (включая .gguf)
                                    filePickerLauncher.launch("*/*")
                                }) {
                                    Text("Выбрать файл")
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            Text(
                                "⚠️ Локальный инференс работает напрямую на устройстве через llama.cpp. " +
                                "Требуется модель в формате GGUF (рекомендуется quantized Q4_K_M для баланса скорости и качества).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Настройка модели (для облачных провайдеров)
                if (selectedProvider.value != "local") {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Модель",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    currentModel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { showCustomModelDialog = true }) {
                                Text("Изменить")
                            }
                        }
                    }
                }
                
                // Кастомный API URL (только для custom провайдера)
                if (selectedProvider.value == "custom") {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "API URL",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    customApiUrl.value.ifEmpty { "Не задан" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { showCustomApiUrlDialog = true }) {
                                Text("Изменить")
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Spacer(Modifier.height(16.dp))
                
                // API ключ (не нужен для локальной модели)
                if (selectedProvider.value != "local") {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (apiKey.isEmpty()) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (apiKey.isEmpty()) "API ключ не установлен" else "API ключ установлен",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        if (apiKey.isEmpty()) {
                                            "Получите бесплатный ключ на ai.google.dev"
                                        } else {
                                            "Ключ: ${apiKey.take(10)}..."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { showApiKeyDialog = true }) {
                                    Text("Изменить")
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }
                
                Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                // Дополнительные настройки
                Text(
                    "Дополнительно",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showBedTimeDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    ListItem(
                        headlineContent = { Text("Время отхода ко сну") },
                        supportingContent = { Text("Для составления персонального плана") },
                        leadingContent = {
                            Icon(Icons.Default.Bedtime, null)
                        },
                        trailingContent = {
                            Text(
                                String.format("%02d:%02d", bedTimeHour.intValue, bedTimeMinute.intValue),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
                
                SwitchPreference(
                    title = "Автозапись уроков",
                    description = "Создавать конспекты автоматически (экспериментально)",
                    listenState = autoRecordLectures
                ) {
                    if (it) {
                        // Проверяем разрешение при включении
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (hasPermission) {
                            autoRecordLectures.value = true
                            activity.mainPrefs.save("ai_auto_record_lectures" to true)
                        } else {
                            // Запрашиваем разрешение
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        autoRecordLectures.value = false
                        activity.mainPrefs.save("ai_auto_record_lectures" to false)
                    }
                }
                
                Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                // Информация
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                "О AI помощнике",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "AI помощник использует Google Gemini для помощи с домашними заданиями, " +
                                "анализа успеваемости и составления персонального плана учёбы. " +
                                "Все данные обрабатываются через защищённое соединение.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Диалог кастомного API URL
    if (showCustomApiUrlDialog) {
        var newUrl by remember { mutableStateOf(customApiUrl.value) }
        
        AlertDialog(
            onDismissRequest = { showCustomApiUrlDialog = false },
            title = { Text("Кастомный API URL") },
            text = {
                Column {
                    Text(
                        "Введите полный URL вашего AI API (например, https://api.yourserver.com/v1)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("API URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://...") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    customApiUrl.value = newUrl
                    activity.mainPrefs.save("ai_custom_api_url" to newUrl)
                    showCustomApiUrlDialog = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomApiUrlDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Диалог кастомной модели
    if (showCustomModelDialog) {
        var newModel by remember { mutableStateOf(customModel.value.ifEmpty { defaultModels[selectedProvider.value] ?: "" }) }
        
        AlertDialog(
            onDismissRequest = { showCustomModelDialog = false },
            title = { Text("Модель ${providers.find { it.first == selectedProvider.value }?.second}") },
            text = {
                Column {
                    Text(
                        when (selectedProvider.value) {
                            "google" -> "Например: gemini-2.0-flash-exp, gemini-exp-1206, gemini-1.5-pro-002"
                            "openai" -> "Например: gpt-4o, gpt-4-turbo, gpt-3.5-turbo"
                            else -> "Укажите название модели для вашего API"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newModel,
                        onValueChange = { newModel = it },
                        label = { Text("Модель") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    customModel.value = newModel
                    activity.mainPrefs.save("ai_custom_model" to newModel)
                    showCustomModelDialog = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomModelDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Диалог выбора времени сна
    if (showBedTimeDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = bedTimeHour.intValue,
            initialMinute = bedTimeMinute.intValue,
            is24Hour = true
        )
        
        var tempHour by remember { mutableIntStateOf(bedTimeHour.intValue) }
        var tempMinute by remember { mutableIntStateOf(bedTimeMinute.intValue) }
        
        AlertDialog(
            onDismissRequest = { showBedTimeDialog = false },
            title = { Text("Время отхода ко сну") },
            text = {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Часы
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { 
                            tempHour = (tempHour + 1) % 24 
                        }) {
                            Icon(Icons.Default.KeyboardArrowUp, null)
                        }
                        Text(
                            String.format("%02d", tempHour),
                            style = MaterialTheme.typography.displaySmall
                        )
                        IconButton(onClick = { 
                            tempHour = if (tempHour - 1 < 0) 23 else tempHour - 1
                        }) {
                            Icon(Icons.Default.KeyboardArrowDown, null)
                        }
                    }
                    
                    Text(
                        ":",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Минуты
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { 
                            tempMinute = (tempMinute + 15) % 60
                        }) {
                            Icon(Icons.Default.KeyboardArrowUp, null)
                        }
                        Text(
                            String.format("%02d", tempMinute),
                            style = MaterialTheme.typography.displaySmall
                        )
                        IconButton(onClick = { 
                            tempMinute = if (tempMinute - 15 < 0) 45 else tempMinute - 15
                        }) {
                            Icon(Icons.Default.KeyboardArrowDown, null)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    bedTimeHour.intValue = tempHour
                    bedTimeMinute.intValue = tempMinute
                    activity.mainPrefs.save("ai_bed_time_hour" to tempHour)
                    activity.mainPrefs.save("ai_bed_time_minute" to tempMinute)
                    showBedTimeDialog = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBedTimeDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Диалог изменения API ключа
    if (showApiKeyDialog) {
        var newKey by remember { mutableStateOf(apiKey) }
        
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("API ключ Gemini") },
            text = {
                Column {
                    Text("Получите бесплатный ключ на ai.google.dev")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("API ключ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    GeminiService.setApiKey(context, newKey)
                    apiKey = newKey
                    showApiKeyDialog = false
                }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}



