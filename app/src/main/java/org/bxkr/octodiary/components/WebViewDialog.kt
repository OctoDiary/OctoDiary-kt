package org.bxkr.octodiary.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.R
import org.bxkr.octodiary.automation.AutomationEngine
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewDialog(
    url: String,
    onDismissRequest: () -> Unit,
    actionPlanJson: String? = null,
    automationTask: String? = null
) {
    val context = LocalContext.current
    var currentUrl = remember { url }
    var isLoading by remember { mutableStateOf(true) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val scope = rememberCoroutineScope()
    
    // Состояние автоматизации
    var automationRunning by remember { mutableStateOf(false) }
    val automationLogs = remember { mutableStateOf<List<String>>(emptyList()) }
    val automationEngine = remember { mutableStateOf<AutomationEngine?>(null) }
    
    // Проверяем, настроена ли автоматизация
    val automationConfigured = remember {
        val provider = context.mainPrefs.get<String>("automation_provider")
        val apiKey = context.mainPrefs.get<String>("automation_api_key")
        !provider.isNullOrBlank() && provider != "disabled" && !apiKey.isNullOrBlank()
    }

    
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = { onDismissRequest() }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.mes_material)) },
                    navigationIcon = {
                        IconButton(onClick = { onDismissRequest() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        val clipboardManager =
                            LocalClipboardManager.current
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text(stringResource(id = R.string.copy_link))
                                }
                            },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(currentUrl))
                            }) {
                                Icon(
                                    Icons.Rounded.Link,
                                    stringResource(R.string.copy_link)
                                )
                            }
                        }
                        IconButton(onClick = {
                            val js = """
                                (function(){
                                  function text(el){return (el.innerText||el.textContent||'').trim().toLowerCase();}
                                  function clickable(node){return node.closest('button,[role=\"button\"],.MuiButtonBase-root,.MuiButton-root,.btn,.Button');}
                                  var labels = ['далее','дальше','продолжить','next','continue'];
                                  var cands = Array.from(document.querySelectorAll('button, a[role=\"button\"], [role=\"button\"], .MuiButtonBase-root, .MuiButton-root, .btn, .Button'));
                                  for(var i=0;i<cands.length;i++){var el=cands[i];var t=text(el);if(labels.includes(t) || labels.some(x=>t.startsWith(x))){el.click();return true;}}
                                  var aria = document.querySelector('[aria-label=\"Далее\"], [aria-label=\"Next\"], [title=\"Далее\"], [title=\"Next\"]');
                                  if(aria){aria.click();return true;}
                                  var span = Array.from(document.querySelectorAll('span, p, div')).find(function(n){var t=text(n);return ['далее','дальше','продолжить','next','continue'].includes(t);});
                                  if(span){var btn = clickable(span); if(btn){btn.click(); return true;}}
                                  return false;
                                })();
                            """.trimIndent()
                            webViewRef.value?.evaluateJavascript(js, null)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = stringResource(R.string.next)
                            )
                        }
                        
                        // Кнопка автоматизации (только если настроена и есть задача)
                        if (automationConfigured && !automationTask.isNullOrBlank()) {
                            IconButton(onClick = {
                                if (automationRunning) {
                                    // Остановить автоматизацию
                                    automationEngine.value?.stop()
                                    automationRunning = false
                                } else {
                                    // Запустить автоматизацию
                                    val wv = webViewRef.value
                                    if (wv != null) {
                                        automationLogs.value = emptyList()
                                        automationRunning = true
                                        
                                        val engine = AutomationEngine(
                                            context = context,
                                            webView = wv,
                                            task = automationTask,
                                            onProgress = { log ->
                                                automationLogs.value = automationLogs.value + log
                                            },
                                            onComplete = { result ->
                                                automationLogs.value = automationLogs.value + "✅ $result"
                                                automationRunning = false
                                            }
                                        )
                                        automationEngine.value = engine
                                        
                                        scope.launch {
                                            engine.start()
                                        }
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (automationRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = if (automationRunning) "Остановить" else "Запустить автоматизацию",
                                    tint = if (automationRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
                // WebView
                Surface(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Column {
                        if (isLoading) {
                            LinearProgressIndicator()
                        }
                        AndroidView(
                            factory = { ctx ->
                                ctx.webViewFactory(url,
                                    onUrlChange = { newUrl -> currentUrl = newUrl },
                                    onLoadState = { loading -> isLoading = loading }
                                ).also { webViewRef.value = it }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Overlay с логами автоматизации
                if (automationLogs.value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .width(300.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.85f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = if (automationRunning) "🤖 Автоматизация активна" else "🤖 Автоматизация",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(Modifier.height(8.dp))
                                
                                val listState = rememberLazyListState()
                                LaunchedEffect(automationLogs.value.size) {
                                    if (automationLogs.value.isNotEmpty()) {
                                        listState.animateScrollToItem(automationLogs.value.size - 1)
                                    }
                                }
                                
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.height(200.dp)
                                ) {
                                    items(automationLogs.value.takeLast(20)) { log ->
                                        Text(
                                            text = log,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
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
}

@SuppressLint("SetJavaScriptEnabled")
private fun Context.webViewFactory(
    url: String,
    onUrlChange: (String) -> Unit,
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
            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                onUrlChange(url ?: "")
                super.doUpdateVisitedHistory(view, url, isReload)
            }
        }
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        val cookieHost = if (DataService.subsystem == Diary.MES) ".mos.ru" else ".mosreg.ru"
        CookieManager.getInstance().apply {
            setCookie(cookieHost, "aupd_token=${DataService.token}")
            setCookie(cookieHost, "aupd_current_role=2:1")
        }
        loadUrl(url)
    }
}