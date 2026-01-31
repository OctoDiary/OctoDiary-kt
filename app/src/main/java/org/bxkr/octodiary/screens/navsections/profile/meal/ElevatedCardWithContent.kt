package org.bxkr.octodiary.screens.navsections.profile.meal


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R

@Composable
fun ElevatedCardWithContent(
    onClick: () -> Unit,
    title: @Composable RowScope.() -> Unit,
    icons: @Composable RowScope.() -> Unit = {},
    rotation: Float = 0f,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        Modifier.padding(bottom = 16.dp),
    ) {
        Column(Modifier.clickable { onClick() }) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .weight(1f, false)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    title()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    icons()
                    Icon(
                        Icons.Default.ArrowDropDown,
                        stringResource(R.string.expand),
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .rotate(
                                animateFloatAsState(
                                    targetValue = rotation,
                                    animationSpec = tween(600),
                                    label = "rotate_anim"
                                ).value
                            )
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

            }
            content()
        }
    }
}


