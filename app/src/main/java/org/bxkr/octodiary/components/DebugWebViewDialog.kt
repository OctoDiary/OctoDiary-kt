package org.bxkr.octodiary.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.R
import org.bxkr.octodiary.ai.HeadlessWebViewController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugWebViewDialog(
    url: String,
    onDismissRequest: () -> Unit,
    aiResponses: List<String>,
    onAiResponse: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val headlessController = remember { HeadlessWebViewController(context) }
    
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onDismissRequest
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // WebView фон
            Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug WebView") },
                navigationIcon = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Закрыть")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        headlessController.start(url, "Проанализируй страницу и выполни задание", 
                            onDone = { result ->
                                onAiResponse("RESULT: $result")
                            },
                            onAiResponse = { response ->
                                onAiResponse(response)
                            }
                        )
                    }) {
                        Text("Start AI")
                    }
                }
            )
        }
            ) { padding ->
                Surface(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Column {
                        if (isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        AndroidView(
                            factory = { ctx ->
                                ctx.webViewFactory(url,
                                    onLoadState = { loading -> isLoading = loading }
                                ).also { webViewRef.value = it }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            // AI Responses Overlay
            if (aiResponses.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .heightIn(max = 400.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "AI Responses",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(aiResponses.takeLast(10)) { response ->
                                    Text(
                                        text = response,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .background(
                                                Color.Green.copy(alpha = 0.3f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun Context.webViewFactory(
    url: String,
    onLoadState: (Boolean) -> Unit
): WebView {
    return WebView(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                onLoadState(false)
                super.onPageFinished(view, url)
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                onLoadState(true)
                super.onPageStarted(view, url, favicon)
            }
        }
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        val cookieHost = if (DataService.subsystem == Diary.MES) ".mos.ru" else ".mosreg.ru"
        CookieManager.getInstance().apply {
            setCookie(cookieHost, "aupd_token=${DataService.token}")
            setCookie(cookieHost, "aupd_current_role=2:1")
        }
        loadUrl(url)
    }
}