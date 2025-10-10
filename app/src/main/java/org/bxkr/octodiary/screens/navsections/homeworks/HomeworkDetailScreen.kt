package org.bxkr.octodiary.screens.navsections.homeworks

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Button
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
import org.bxkr.octodiary.ai.AiManager
import org.bxkr.octodiary.ai.AiSolution
import org.bxkr.octodiary.ai.AiSolutionStore
import org.bxkr.octodiary.ai.HeadlessWebViewController
import org.bxkr.octodiary.components.WebViewDialog
import org.bxkr.octodiary.components.DebugWebViewDialog
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.navControllerLive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDetailScreen(entryStudentId: Long) {
    val context = LocalContext.current
    val nav = navControllerLive.value
    val hw = remember(entryStudentId) {
        DataService.homeworks.firstOrNull { it.homeworkEntryStudentId == entryStudentId }
    }
    val openWeb = remember { mutableStateOf(false) }
    val webUrl = remember { mutableStateOf("") }
    val actionPlan = remember { mutableStateOf<String?>(null) }
    val extractedHtml = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val headlessController = remember { HeadlessWebViewController(context) }
    val showDebugWebView = remember { mutableStateOf(false) }
    val debugWebViewUrl = remember { mutableStateOf("") }
    val aiResponses = remember { mutableStateOf<List<String>>(emptyList()) }

    if (openWeb.value) {
        WebViewDialog(
            url = webUrl.value,
            onDismissRequest = { openWeb.value = false },
            actionPlanJson = actionPlan.value,
            onHtmlExtracted = { html ->
                extractedHtml.value = html
            }
        )
    }
    
    if (showDebugWebView.value) {
        DebugWebViewDialog(
            url = debugWebViewUrl.value,
            onDismissRequest = { showDebugWebView.value = false },
            aiResponses = aiResponses.value,
            onAiResponse = { response ->
                aiResponses.value = aiResponses.value + response
            }
        )
    }

    Scaffold(topBar = {
        MediumTopAppBar(
            title = { Text(hw?.subjectName ?: stringResource(R.string.homework)) },
            navigationIcon = {
                IconButton(onClick = { nav?.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back))
                }
            },
            actions = {
                IconButton(onClick = { 
                    if (hw?.materials?.isNotEmpty() == true) {
                        val material = hw.materials.first()
                        if (material.type == "attachments" || context.isDemo) {
                            val url = material.urls.firstOrNull { true }?.toString()
                            if (url != null) {
                                debugWebViewUrl.value = url
                                showDebugWebView.value = true
                            }
                        } else {
                            DataService.getLaunchUrl(hw.homeworkEntryId, material.uuid) { url ->
                                debugWebViewUrl.value = url
                                showDebugWebView.value = true
                            }
                        }
                    }
                }) {
                    Icon(Icons.Rounded.BugReport, "Debug WebView")
                }
            }
        )
    }) { padding ->
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
                                val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                                ContextCompat.startActivity(context, browserIntent, null)
                            } else {
                                DataService.getLaunchUrl(hw.homeworkEntryId, material.uuid) { url ->
                                    webUrl.value = url
                                    openWeb.value = true
                                }
                            }
                        }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = stringResource(R.string.open))
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
                                        client.newCall(req).execute().use { it.code() != 404 }
                                    } catch (_: Throwable) { false }
                                }
                                val finalUrl = if (ok) primary else "$subjectHost/test?id=$id"
                                val browserIntent = Intent(Intent.ACTION_VIEW, finalUrl.toUri())
                                ContextCompat.startActivity(context, browserIntent, null)
                            }
                        }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = label)
                            Text("  " + label)
                        }
                    }
                }
            }

            HorizontalDivider()

            // AI Solve Button
            if (AiManager.isConfigured(context)) {
                var solving by remember { mutableStateOf(false) }
                var countdown by remember { mutableStateOf(0) }
                var currentSolution by remember { mutableStateOf<AiSolution?>(null) }
                
                Button(
                    onClick = {
                        scope.launch {
                            solving = true
                            val provider = AiManager.getProvider(context)
                            val model = AiManager.getSelectedModel(context, provider)
                            
                            // Build prompt with HTML if available
                            val taskText = buildString {
                                append("${hw.subjectName}: ${hw.homework}\n${hw.description}")
                                if (extractedHtml.value != null) {
                                    append("\n\nHTML структура задания:\n")
                                    append(extractedHtml.value)
                                }
                            }
                            
                            // Try to get URL for headless automation
                            val materials = hw.materials
                            if (materials.isNotEmpty()) {
                                val material = materials.first()
                                android.util.Log.d("HomeworkDetail", "Found material for automation: ${material.title}")
                                
                                if (material.type == "attachments" || context.isDemo) {
                                    val url = material.urls.firstOrNull { true }?.toString()
                           if (url != null) {
                               android.util.Log.d("HomeworkDetail", "Using attachment URL: $url")
                               headlessController.start(url, taskText, 
                                   onDone = { result ->
                                       android.util.Log.d("HomeworkDetail", "Headless automation completed: $result")
                                       scope.launch {
                                           val solution = AiSolution(
                                               provider = "Headless-AI",
                                               model = model,
                                               text = "Автоматизация завершена: $result",
                                               createdAt = System.currentTimeMillis(),
                                               estimatedSeconds = 0
                                           )
                                           AiSolutionStore.append(context, entryStudentId, solution)
                                           currentSolution = solution
                                           solving = false
                                           countdown = 0
                                       }
                                   },
                                   onAiResponse = { response ->
                                       aiResponses.value = aiResponses.value + response
                                   }
                               )
                                    } else {
                                        android.util.Log.w("HomeworkDetail", "No attachment URL found, using fallback")
                                        val solutionText = provider.solve(context, taskText, model)
                                        val solution = AiSolution(
                                            provider = "OpenAI-compatible",
                                            model = model,
                                            text = solutionText,
                                            createdAt = System.currentTimeMillis(),
                                            estimatedSeconds = 0
                                        )
                                        AiSolutionStore.append(context, entryStudentId, solution)
                                        currentSolution = solution
                                        solving = false
                                        countdown = 0
                                    }
                                } else {
                                    // Get launch URL for non-attachment materials
                                    android.util.Log.d("HomeworkDetail", "Getting launch URL for material")
                           DataService.getLaunchUrl(hw.homeworkEntryId, material.uuid) { url ->
                               android.util.Log.d("HomeworkDetail", "Got launch URL: $url")
                               headlessController.start(url, taskText, 
                                   onDone = { result ->
                                       android.util.Log.d("HomeworkDetail", "Headless automation completed: $result")
                                       scope.launch {
                                           val solution = AiSolution(
                                               provider = "Headless-AI",
                                               model = model,
                                               text = "Автоматизация завершена: $result",
                                               createdAt = System.currentTimeMillis(),
                                               estimatedSeconds = 0
                                           )
                                           AiSolutionStore.append(context, entryStudentId, solution)
                                           currentSolution = solution
                                           solving = false
                                           countdown = 0
                                       }
                                   },
                                   onAiResponse = { response ->
                                       aiResponses.value = aiResponses.value + response
                                   }
                               )
                           }
                                }
                            } else {
                                android.util.Log.w("HomeworkDetail", "No materials found for automation, using fallback")
                                // Fallback to old method if no URL
                                val solutionText = provider.solve(context, taskText, model)
                                val solution = AiSolution(
                                    provider = "OpenAI-compatible",
                                    model = model,
                                    text = solutionText,
                                    createdAt = System.currentTimeMillis(),
                                    estimatedSeconds = 0
                                )
                                AiSolutionStore.append(context, entryStudentId, solution)
                                currentSolution = solution
                                solving = false
                                countdown = 0
                            }
                        }
                    },
                    enabled = !solving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (solving && countdown > 0) {
                        Text(stringResource(R.string.ai_time_remaining, countdown.toString()))
                    } else if (solving) {
                        Text(stringResource(R.string.ai_solve) + "...")
                    } else {
                        Text(stringResource(R.string.ai_solve))
                    }
                }

                // Current solution
                currentSolution?.let { sol ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.ai_solution),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${sol.model} • ${sol.estimatedSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(sol.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Previous solutions
                val prevSolutions = remember { AiSolutionStore.load(context, entryStudentId) }
                if (prevSolutions.isNotEmpty()) {
                    Text(
                        stringResource(R.string.ai_prev_solutions),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    prevSolutions.forEach { sol ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${sol.model} • ${sol.estimatedSeconds}s", style = MaterialTheme.typography.bodySmall)
                                Text(sol.text, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
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
