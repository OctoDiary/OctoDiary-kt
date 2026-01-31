package org.bxkr.octodiary.screens.navsections.homeworks


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.navControllerLive
import org.bxkr.octodiary.components.WebViewDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDetailScreen(entryStudentId: Long) {
    var showWebView by rememberSaveable { mutableStateOf(false) }
    var webViewUrl by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val nav = navControllerLive.value
    val hw = remember(entryStudentId) {
        DataService.homeworks.firstOrNull { it.homeworkEntryStudentId == entryStudentId }
    }
    val scope = rememberCoroutineScope()
    var showAiChat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(hw?.subjectName ?: stringResource(R.string.homework)) },
                navigationIcon = {
                    IconButton(onClick = { nav?.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            if (hw != null) {
                FloatingActionButton(
                    onClick = { showAiChat = true }
                ) {
                    Icon(Icons.Default.Chat, "AI помощник")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (hw == null) {
                Text(stringResource(R.string.no_event))
                return@Column
            }

            // Header and checkbox
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val isDone = rememberSaveable { mutableStateOf(hw.isDone) }
                Checkbox(isDone.value, onCheckedChange = { checked ->
                    isDone.value = checked
                    scope.launch {
                        DataService.setHomeworkDoneState(hw.homeworkEntryStudentId, checked) {}
                    }
                })
                Text(
                    hw.description,
                    Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = hw.homework,
                style = MaterialTheme.typography.bodyLarge
            )

            HorizontalDivider()

            // Materials (ЦДЗ)
            if (hw.materials.isNotEmpty()) {
                hw.materials.forEach { material ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(material.title, style = MaterialTheme.typography.titleSmall)
                            Text(material.typeName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        
                        OutlinedButton(onClick = {
                            if (material.type == "attachments" || context.isDemo) {
                                val url = material.urls.firstOrNull { true }?.toString() ?: return@OutlinedButton
                                webViewUrl = url
                                showWebView = true
                            } else {
                                DataService.getLaunchUrl(hw.homeworkEntryId, material.uuid) { url ->
                                    webViewUrl = url
                                    showWebView = true
                                }
                            }
                        }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                            Icon(Icons.AutoMirrored.Default.OpenInNew, contentDescription = stringResource(R.string.open))
                            Text("  " + stringResource(R.string.open))
                        }
                    }
                }
            }

            // Auto-link for РЕШУ ОГЭ/ЕГЭ
            run {
                val text = (hw.description + "\n" + hw.homework)
                val idMatch = Regex("""(\d{6,9})""").find(text)
                val id = idMatch?.groupValues?.getOrNull(1)
                if (id != null) {
                    val isEge = text.contains("ЕГЭ", ignoreCase = true)
                    val isOge = text.contains("ОГЭ", ignoreCase = true)
                    val label = when {
                        isEge -> stringResource(R.string.reshu_ege)
                        isOge -> stringResource(R.string.reshu_oge)
                        else -> null
                    }
                    if (label != null) {
                        val client = remember { OkHttpClient() }
                        val subjectSlug = subjectSlugFromName(hw.subjectName)
                        val baseHost = if (isEge) "https://ege.sdamgia.ru" else "https://oge.sdamgia.ru"
                        val subjectHost = "https://$subjectSlug-${if (isEge) "ege" else "oge"}.sdamgia.ru"
                        OutlinedButton(onClick = {
                            scope.launch {
                                val primary = "$baseHost/test?id=$id"
                                val ok = withContext(Dispatchers.IO) {
                                    try {
                                        val req = Request.Builder().head().url(primary).build()
                                        client.newCall(req).execute().use { it.code != 404 }
                                    } catch (_: Throwable) { false }
                                }
                                val finalUrl = if (ok) primary else "$subjectHost/test?id=$id"
                                webViewUrl = finalUrl
                                showWebView = true
                            }
                        }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                            Icon(Icons.AutoMirrored.Default.OpenInNew, contentDescription = label)
                            Text("  " + label)
                        }
                    }
                }
            }

            HorizontalDivider()
        }
    }
    
    // AI чат диалог
    if (showAiChat && hw != null) {
        org.bxkr.octodiary.components.ai.HomeworkAiChatDialog(
            homework = hw,
            onDismiss = { showAiChat = false }
        )
    }
    
    // WebView для открытия ссылок внутри приложения
    if (showWebView) {
        WebViewDialog(
            url = webViewUrl,
            onDismiss = { showWebView = false }
        )
    }
}

private fun subjectSlugFromName(name: String): String = when {
    name.contains("матем", ignoreCase = true) -> "math"
    name.contains("информ", ignoreCase = true) -> "inf"
    name.contains("русс", ignoreCase = true) -> "rus"
    name.contains("литерат", ignoreCase = true) -> "lit"
    name.contains("физик", ignoreCase = true) -> "phys"
    name.contains("хими", ignoreCase = true) -> "chem"
    name.contains("биол", ignoreCase = true) -> "bio"
    name.contains("истор", ignoreCase = true) -> "hist"
    name.contains("обще", ignoreCase = true) -> "soc"
    name.contains("географ", ignoreCase = true) -> "geo"
    name.contains("англ", ignoreCase = true) -> "en"
    name.contains("немец", ignoreCase = true) -> "de"
    name.contains("франц", ignoreCase = true) -> "fr"
    else -> "math"
}



