package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Link
import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.view.MotionEvent
import org.bxkr.octodiary.mainPrefs
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalContext
import org.bxkr.octodiary.MainPrefs
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WebViewDialog(
    url: String,
    onDismissRequest: () -> Unit,
    actionPlanJson: String? = null,
    onHtmlExtracted: ((String) -> Unit)? = null,
    onScreenshot: ((Bitmap) -> Unit)? = null,
    aiVisualMode: Boolean = false,
    aiPrompt: String = ""
) {
    val ctx = LocalContext.current
    var currentUrl = remember { url }
    var isLoading by remember { mutableStateOf(true) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val scope = rememberCoroutineScope()
    val prefs = MainPrefs(ctx)
    val model = prefs.ctx.getSharedPreferences(prefs.prefPath, android.content.Context.MODE_PRIVATE)
    .getString("ai_model", "gpt-4o") ?: "gpt-4o"

    // Extract HTML when page loads
    LaunchedEffect(isLoading) {
        if (!isLoading && webViewRef.value != null && onHtmlExtracted != null) {
            webViewRef.value?.evaluateJavascript(
                "(function() { return document.documentElement.outerHTML; })();"
            ) { html ->
                onHtmlExtracted(html?.removeSurrounding("\"")?.replace("\\n", "\n")?.replace("\\\"", "\"") ?: "")
            }
        }
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
                                Icons.AutoMirrored.Default.ArrowBack,
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
                                    Icons.Default.Link,
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
                                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                                contentDescription = stringResource(R.string.next)
                            )
                        }
                        // Кнопка для скриншота — появится при long-press на заголовке (для теста)
                        IconButton(
                            onClick = {},
                            modifier = Modifier.combinedClickable(
                                onLongClick = {
                                    val wv = webViewRef.value
                                    if (wv != null) {
                                        val bitmap = wv.drawToBitmap()
                                        onScreenshot?.invoke(bitmap)
                                        // Для примера сохраняем скриншот в Pictures/OctoDiary_Screenshots
                                        val filename = "webviewshot_${System.currentTimeMillis()}.png"
                                        val dir = java.io.File(ctx.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "OctoDiary_Screenshots")
                                        dir.mkdirs()
                                        val file = java.io.File(dir, filename)
                                        val out = java.io.FileOutputStream(file)
                                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                                        out.flush()
                                        out.close()
                                        android.widget.Toast.makeText(ctx, "Скриншот сохранён: ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                },
                                onClick = {}
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Camera, contentDescription = "Скриншот")
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

// Вспомогательная функция — расширение
fun WebView.drawToBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    this.draw(canvas)
    return bmp
}


