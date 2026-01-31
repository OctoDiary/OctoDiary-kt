package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.bxkr.octodiary.ai.AiSettings
import org.bxkr.octodiary.ai.AiSettingsViewModel
import org.bxkr.octodiary.components.SwitchWithText
import org.bxkr.octodiary.navControllerLive // Assuming this is still needed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val nav = navControllerLive.value
    // ViewModel для настроек AI
    val viewModel = remember { AiSettingsViewModel(context) } // Manually pass context for now
    val uiState by viewModel.uiState.collectAsState()

    var selectedProviderExpanded by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки AI") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Раздел "AI Провайдер"
            Text(
                "AI Провайдер",
                style = MaterialTheme.typography.titleMedium
            )

            ExposedDropdownMenuBox(
                expanded = selectedProviderExpanded,
                onExpandedChange = { selectedProviderExpanded = !selectedProviderExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.selectedProvider,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Выбранный провайдер") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedProviderExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = selectedProviderExpanded,
                    onDismissRequest = { selectedProviderExpanded = false }
                ) {
                    viewModel.getAvailableProviders().forEach { providerName ->
                        DropdownMenuItem(
                            text = { Text(providerName) },
                            onClick = {
                                viewModel.saveSettings(uiState.copy(selectedProvider = providerName))
                                selectedProviderExpanded = false
                            }
                        )
                    }
                }
            }

            // Поля ввода для API ключей в зависимости от выбранного провайдера
            when (uiState.selectedProvider) {
                "OpenAI" -> {
                    OutlinedTextField(
                        value = uiState.openaiApiKey,
                        onValueChange = { viewModel.saveSettings(uiState.copy(openaiApiKey = it)) },
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "Gemini" -> {
                    OutlinedTextField(
                        value = uiState.geminiApiKey,
                        onValueChange = { viewModel.saveSettings(uiState.copy(geminiApiKey = it)) },
                                            label = { Text("Gemini API Key") },
                                            leadingIcon = { Icon(Icons.Default.Key, null) },                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "Anthropic" -> {
                    OutlinedTextField(
                        value = uiState.anthropicApiKey,
                        onValueChange = { viewModel.saveSettings(uiState.copy(anthropicApiKey = it)) },
                        label = { Text("Anthropic API Key") },
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "Custom" -> {
                    OutlinedTextField(
                        value = uiState.customApiKey,
                        onValueChange = { viewModel.saveSettings(uiState.copy(customApiKey = it)) },
                        label = { Text("Custom API Key") },
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.customBaseUrl,
                        onValueChange = { viewModel.saveSettings(uiState.copy(customBaseUrl = it)) },
                        label = { Text("Custom Base URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.customModel,
                        onValueChange = { viewModel.saveSettings(uiState.copy(customModel = it)) },
                        label = { Text("Custom Model Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Другие настройки AI (пока заглушки)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Дополнительные настройки",
                style = MaterialTheme.typography.titleMedium
            )

            SwitchWithText(
                text = { Text("Автоматическая запись лекций") },
                checked = false, // uiState.autoRecordLectures,
                onCheckedChange = { /* viewModel.saveSettings(uiState.copy(autoRecordLectures = it)) */ }
            )
            Text("Placeholder for other AI settings.", style = MaterialTheme.typography.bodySmall)
        }
    }
}



