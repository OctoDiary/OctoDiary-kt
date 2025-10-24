package org.bxkr.octodiary.screens.navsections.dashboard

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.BuildConfig
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.ChangelogDialog
import org.bxkr.octodiary.components.changelog.Changelog
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChangelogCard(context: Context) {
    val background = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
    )
    var isCardShown by remember { mutableStateOf(context.getInitialIsShown()) }
    var isDialogShown by remember { mutableStateOf(false) }
    val changelog = Changelog.currentChangelog
    AnimatedVisibility(isCardShown, Modifier.padding(vertical = 8.dp)) {
        Row(
            Modifier
                .background(
                    background,
                    MaterialTheme.shapes.extraLarge
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = {
                        context.saveNewVersion()
                        isCardShown = false
                    },
                    onClick = {
                        context.saveNewVersion()
                        isCardShown = false
                        if (changelog.elements != null) {
                            isDialogShown = true
                        }
                    }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(R.string.changelog),
                        Modifier.padding(end = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(changelog.versionShortname),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .padding(vertical = 4.dp)
                            .background(DividerDefaults.color, shape = CircleShape)
                    )
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(stringResource(changelog.shortDescription))
                    }
                }
                if (changelog.elements != null)
                Text(
                    stringResource(R.string.click_to_show_more),
                    Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (changelog.elements != null)
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                stringResource(R.string.next),
                Modifier
                    .padding(end = 8.dp)
                    .size(32.dp),
                MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
    AnimatedVisibility(isDialogShown) {
        ChangelogDialog { isDialogShown = false }
    }
}

private fun Context.getInitialIsShown() =
    (mainPrefs.get<Int>("read_changelog_version") ?: 25) < BuildConfig.VERSION_CODE

private fun Context.saveNewVersion() =
    mainPrefs.save("read_changelog_version" to BuildConfig.VERSION_CODE)