package org.bxkr.octodiary.components.settings


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.BuildConfig
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.ChangelogDialog
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.network.Developer
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.network.NetworkService.ExternalIntegrationConfig.CONTRIBUTORS_GITHUB_URL
import org.bxkr.octodiary.network.NetworkService.ExternalIntegrationConfig.PYTHON_LIB_URL
import org.bxkr.octodiary.network.NetworkService.ExternalIntegrationConfig.TELEGRAM_BOT_URL
import org.bxkr.octodiary.openUri
import org.bxkr.octodiary.save
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme

@Composable
fun About() {
    Column {
        Card()
        Developers()
        OtherProjects()
    }
}

@Composable
private fun Card() {
    var isChangelogDialogShown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var isDebug by remember {
                mutableStateOf(
                    context.mainPrefs.get<Boolean>("force_debug") ?: false
                )
            }
            Row {
                Box {
                    Box(
                        Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .clickable {
                                isDebug = !isDebug
                                context.mainPrefs.save("force_debug" to isDebug)
                            }
                            .background(MaterialTheme.colorScheme.run { if (isDebug) onSecondary else secondary })
                    )
                    Icon(
                        painterResource(R.drawable.ic_launcher_foreground),
                        stringResource(R.string.app_name),
                        Modifier.size(64.dp),
                        MaterialTheme.colorScheme.run { if (isDebug) secondary else onSecondary }
                    )
                }
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.Center) {
                    Text(
                        stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        Text(
                            BuildConfig.VERSION_NAME,
                            Modifier.padding(end = 2.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            stringResource(R.string.changes_label),
                            Modifier.clickable {
                                isChangelogDialogShown = true
                            },
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            val context = LocalContext.current
            Row {
                OutlinedIconButton(onClick = {
                    context.openUri(NetworkService.ExternalIntegrationConfig.GITHUB_REPO_URL)
                }) {
                    Icon(
                        Icons.Default.Code,
                        "GitHub"
                    )
                }
                OutlinedIconButton(onClick = {
                    context.openUri(NetworkService.ExternalIntegrationConfig.TELEGRAM_CHANNEL_URL)
                }) {
                    Icon(
                        Icons.Default.Campaign,
                        "Telegram"
                    )
                }
            }
        }
    }
    AnimatedVisibility(isChangelogDialogShown) {
        ChangelogDialog { isChangelogDialogShown = false }
    }
}

@Composable
private fun Developers() {
    Row(
        Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f))
        Text(
            stringResource(R.string.developers),
            Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        HorizontalDivider(Modifier.weight(1f))
    }
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Developer.developers.forEach {
                DevelopersGridItem(it)
            }
        }
        val bottomBoxShape = MaterialTheme.shapes.extraSmall.copy(
            bottomEnd = MaterialTheme.shapes.extraLarge.bottomEnd,
            bottomStart = MaterialTheme.shapes.extraLarge.bottomStart
        )
        val context = LocalContext.current
        Box(
            Modifier
                .padding(top = 2.dp)
                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, bottomBoxShape)
                .background(Color.Transparent)
                .fillMaxWidth()
                .clickable { context.openUri(CONTRIBUTORS_GITHUB_URL) }
        ) {
            Text(
                stringResource(R.string.and_other_contributors),
                Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun RowScope.DevelopersGridItem(developer: Developer) {
    val context = LocalContext.current
    Card(
        Modifier
            .weight(1f),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults
            .cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                developer.nickname,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp),
                minLines = 2,
                maxLines = 2
            )
            Row {
                if (developer.githubLink != null) {
                    FilledTonalIconButton(onClick = { context.openUri(developer.githubLink) }) {
                        Icon(
                            painterResource(id = R.drawable.github_24),
                            "GitHub",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                if (developer.telegramLink != null) {
                    FilledTonalIconButton(onClick = { context.openUri(developer.telegramLink) }) {
                        Icon(
                            painterResource(id = R.drawable.telegram_24),
                            "Telegram",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherProjects() {
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text(
                stringResource(R.string.other_octodiary_projects),
                Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider(Modifier.weight(1f))
        }
        val context = LocalContext.current
        Card(
            colors = CardDefaults.cardColors(Color.Transparent),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        0.5f to MaterialTheme.colorScheme.surfaceContainer,
                        1f to MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    MaterialTheme.shapes.extraLarge
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    context.openUri(TELEGRAM_BOT_URL)
                }
        ) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        stringResource(R.string.bot),
                        Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                    )
                    Column {
                        Text(
                            stringResource(R.string.bot),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.bot_description),
                            Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Default.ArrowForward,
                    stringResource(R.string.open),
                    Modifier
                        .padding(end = 8.dp)
                        .size(32.dp),
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        Card(
            colors = CardDefaults.cardColors(Color.Transparent),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(
                    Brush.linearGradient(
                        0.5f to MaterialTheme.colorScheme.surfaceContainer,
                        1f to MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    MaterialTheme.shapes.extraLarge
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    context.openUri(PYTHON_LIB_URL)
                }
        ) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Book,
                        stringResource(R.string.bot),
                        Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                    )
                    Column {
                        Text(
                            stringResource(R.string.python_lib),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.python_lib_description),
                            Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Icon(
                    Icons.AutoMirrored.Default.ArrowForward,
                    stringResource(R.string.open),
                    Modifier
                        .padding(end = 8.dp)
                        .size(32.dp),
                    MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Preview
@Composable
fun AboutPreview() {
    OctoDiaryTheme {
        Surface {
            About()
        }
    }
}


