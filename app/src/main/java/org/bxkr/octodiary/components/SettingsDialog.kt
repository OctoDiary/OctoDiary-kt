package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.settings.About
import org.bxkr.octodiary.components.settings.AccessibilitySettings
import org.bxkr.octodiary.components.settings.AiSettings
import org.bxkr.octodiary.components.settings.Appearance
import org.bxkr.octodiary.components.settings.BatterySaverSettings
import org.bxkr.octodiary.components.settings.BellScheduleSettings
import org.bxkr.octodiary.components.settings.Common
import org.bxkr.octodiary.components.settings.Notifications
import org.bxkr.octodiary.components.settings.Security
import org.bxkr.octodiary.launchUrlLive
import org.bxkr.octodiary.logOut
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.network.NetworkService.ExternalIntegrationConfig.TELEGRAM_REPORT_URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(onDismissRequest: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = { onDismissRequest() }
    ) {
        val defaultTitle = stringResource(R.string.settings)
        var selectScreen by remember { mutableStateOf(true) }
        var currentTitle by remember { mutableStateOf(defaultTitle) }
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    title = {
                        if (selectScreen) {
                            Text(stringResource(R.string.settings))
                        } else {
                            Text(currentTitle)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectScreen) {
                                onDismissRequest()
                            } else {
                                selectScreen = true
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        Row(
                            Modifier
                                .padding(16.dp)
                                .clip(CircleShape)
                                .clickable { uriHandler.openUri(TELEGRAM_REPORT_URL) }
                        ) {
                            Row(
                                Modifier
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.report_issue),
                                    Modifier.padding(horizontal = 4.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Icon(
                                    Icons.Default.BugReport,
                                    stringResource(R.string.report_issue),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
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
                Box(
                    Modifier
                        .fillMaxWidth(),
                ) {
                    val activity = LocalActivity.current
                    var currentScreen by remember { mutableStateOf<@Composable () -> Unit>({}) }

                    val enterTransition1 = remember {
                        slideInHorizontally(
                            tween(200)
                        ) { it }
                    }
                    val exitTransition1 = remember {
                        slideOutHorizontally(
                            tween(200)
                        ) { it }
                    }
                    val enterTransition2 = remember {
                        slideInHorizontally(
                            tween(200)
                        ) { -it }
                    }
                    val exitTransition2 = remember {
                        slideOutHorizontally(
                            tween(200)
                        ) { -it }
                    }

                    val SettingsSection: @Composable (icon: ImageVector, title: String, description: String, content: @Composable () -> Unit) -> Unit =
                        { icon, title, description, content ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clickable {
                                        selectScreen = false
                                        currentScreen = content
                                        currentTitle = title
                                    },
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Row(
                                    Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        icon, title,
                                        Modifier
                                            .padding(horizontal = 8.dp)
                                            .size(25.dp)
                                    )
                                    Column {
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 1.em
                                        )
                                        Text(
                                            description,
                                            lineHeight = 1.em,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                    AnimatedVisibility(
                        visible = !selectScreen,
                        enter = enterTransition1,
                        exit = exitTransition1
                    ) {
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            currentScreen()
                        }
                    }
                    AnimatedVisibility(
                        visible = selectScreen,
                        enter = enterTransition2,
                        exit = exitTransition2
                    ) {
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                        ) {
                            SettingsSection(
                                Icons.Default.Settings,
                                stringResource(R.string.common),
                                stringResource(R.string.common_desc),
                            ) { Common() }
                            SettingsSection(
                                Icons.Default.Notifications,
                                stringResource(R.string.notifications),
                                stringResource(R.string.notifications_desc)
                            ) { Notifications() }
                            SettingsSection(
                                Icons.Default.Schedule,
                                "Расписание звонков",
                                "Уведомления о начале/конце уроков с обратным отсчётом"
                            ) { BellScheduleSettings() }
                            SettingsSection(
                                Icons.Default.AutoAwesome,
                                "Настройки AI",
                                "AI помощник, выбор модели, персональный план"
                            ) { AiSettings() }
                            SettingsSection(
                                Icons.Default.Brush,
                                stringResource(R.string.appearance),
                                stringResource(R.string.appearance_desc)
                            ) { Appearance() }
                            SettingsSection(
                                Icons.Default.Lock,
                                stringResource(R.string.security),
                                stringResource(R.string.security_desc)
                            ) { Security() }
                            SettingsSection(
                                Icons.Default.BatteryAlert,
                                "Экономия батареи",
                                "Автоматический режим экономии при низком заряде"
                            ) { BatterySaverSettings() }
                            SettingsSection(
                                Icons.Default.Accessibility,
                                "Специальные возможности",
                                "Режим дислексии, масштаб текста, контрастность"
                            ) { AccessibilitySettings() }
                            SettingsSection(
                                Icons.Default.Info,
                                stringResource(R.string.about),
                                stringResource(R.string.about_desc)
                            ) { About() }

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp), horizontalAlignment = Alignment.End
                            ) {
                                val interactionSource = remember { MutableInteractionSource() }
                                val openBot: (test: Boolean) -> Unit = { isTest ->
                                    val link = Uri.parse(
                                        NetworkService.ExternalIntegrationConfig.BOT_AUTH_URL.format(
                                            DataService.token,
                                            DataService.subsystem.ordinal,
                                            if (isTest) 1 else 0
                                        )
                                    )
                                    launchUrlLive.postValue(link)
                                }
                                LaunchedEffect(interactionSource) {
                                    var isLongClick = false
                                    interactionSource.interactions.collectLatest {
                                        when (it) {
                                            is PressInteraction.Press -> {
                                                isLongClick = false
                                                delay(2000)
                                                isLongClick = true
                                                openBot(true)
                                            }

                                            is PressInteraction.Release -> {
                                                if (!isLongClick) {
                                                    openBot(false)
                                                }
                                            }
                                        }
                                    }
                                }
                                Button(
                                    onClick = {},
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    interactionSource = interactionSource
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Default.OpenInNew,
                                        stringResource(R.string.image),
                                        Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.log_into_bot))
                                }
                                OutlinedButton(onClick = {
                                    onDismissRequest()
                                    activity.logOut("User initiated the log out from settings screen")
                                }) {
                                    Text(stringResource(R.string.log_out))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    selected: Boolean,
    top: Color,
    middle: Color,
    bottom: Color,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    onClick: () -> Unit,
) {
    Box(Modifier.clip(MaterialTheme.shapes.large)) {
        OutlinedCard(modifier.padding(8.dp), shape = MaterialTheme.shapes.extraLarge) {
            Box(Modifier.clickable(onClick = onClick)) {
                Column(
                    Modifier.border(
                        4.dp,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.extraLarge
                    )
                ) {
                    listOf(top, middle, bottom).forEach {
                        AnimatedContent(targetState = it, label = "color_anim") { color ->
                            Box(
                                Modifier
                                    .size(width = 72.dp, height = 36.dp)
                                    .background(color)
                            )
                        }
                    }
                }
                if (showIcon) {
                    Icon(
                        Icons.Default.Image,
                        stringResource(R.string.wallpaper_dynamic_color),
                        Modifier
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(8.dp),
                        MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        AnimatedVisibility(selected) {
            Icon(
                Icons.Default.Done,
                stringResource(R.string.wallpaper_dynamic_color),
                Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .size(18.dp)
                    .padding(2.dp),
                MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}



