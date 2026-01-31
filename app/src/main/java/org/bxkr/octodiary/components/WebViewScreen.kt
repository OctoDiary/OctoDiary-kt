package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.Bitmap
import android.graphics.Canvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bxkr.octodiary.ai.AiHintService

/**
 * WebView для открытия ссылок внутри приложения с AI помощником
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewDialog(
    url: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("Загрузка...") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showHintPanel by remember { mutableStateOf(false) }
    var aiHint by remember { mutableStateOf<String?>(null) }
    var isLoadingHint by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, "Назад")
                        }
                    },
                    actions = {
                        // Кнопка "Подсказка"
                        IconButton(
                            onClick = { showHintPanel = true }
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Получить подсказку",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    title = view?.title ?: url ?: "Загрузка..."
                                }
                            }
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            loadUrl(url)
                            webViewRef = this
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
                
                // Панель с подсказкой от AI
                if (showHintPanel) {
                    HintPanel(
                        hint = aiHint,
                        isLoading = isLoadingHint,
                        onDismiss = { showHintPanel = false },
                        onGetHint = {
                            scope.launch {
                                isLoadingHint = true
                                aiHint = null
                                
                                // Делаем скриншот
                                val screenshot = withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    captureWebViewScreenshot(webViewRef)
                                }
                                
                                if (screenshot == null) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Ошибка создания скриншота",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    isLoadingHint = false
                                    return@launch
                                }
                                
                                // Получаем подсказку от AI
                                val result = AiHintService.getHint(context, url, screenshot)
                                
                                result.onSuccess { hint ->
                                    aiHint = hint
                                    isLoadingHint = false
                                }.onFailure { error ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "Ошибка: ${error.message}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    isLoadingHint = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Панель с подсказками от AI
 */
@Composable
private fun HintPanel(
    hint: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onGetHint: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 8.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "💡 AI Подсказка",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "Закрыть")
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(Modifier.size(32.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "AI анализирует задание...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                hint != null -> {
                    Column {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                hint,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Button(
                            onClick = onGetHint,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Получить новую подсказку")
                        }
                    }
                }
                else -> {
                    Column {
                        Text(
                            "Нажмите кнопку, чтобы получить подсказку от AI",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            "AI не будет решать автоматически, а только даст направление",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Button(
                            onClick = onGetHint,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Получить подсказку")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Создать скриншот WebView
 */
private fun captureWebViewScreenshot(webView: WebView?): Bitmap? {
    return try {
        webView?.let {
            val bitmap = Bitmap.createBitmap(
                it.width,
                it.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            it.draw(canvas)
            bitmap
        }
    } catch (e: Exception) {
        android.util.Log.e("WebView", "Ошибка создания скриншота: ${e.message}")
        null
    }
}



