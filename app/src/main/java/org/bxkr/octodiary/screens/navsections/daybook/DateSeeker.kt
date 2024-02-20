package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.R
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DateSeeker(
    weekPosition: MutableState<Float>,
    dayPosition: PagerState,
    size: Int,
    addWeekBefore: (() -> Unit) -> Unit,
    addWeekAfter: (() -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var sliderValue by remember { mutableFloatStateOf(dayPosition.currentPage.toFloat()) }
    sliderValue = dayPosition.currentPage.toFloat()
    ElevatedCard(
        shape = MaterialTheme.shapes.medium.copy(
            topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)
        )
    ) {
        Column(
            Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.weeks), style = MaterialTheme.typography.labelLarge
            )
            Row {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(id = R.string.add_week_before))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    val loadingFinished = remember { mutableStateOf(true) }
                    IconButton(onClick = {
                        loadingFinished.value = false
                        addWeekBefore { loadingFinished.value = true }
                    }, enabled = loadingFinished.value) {
                        AnimatedVisibility(visible = loadingFinished.value) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                stringResource(id = R.string.add_week_before),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        AnimatedVisibility(visible = !loadingFinished.value) {
                            CircularProgressIndicator(
                                Modifier
                                    .size(20.dp, 20.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
                Slider(
                    modifier = Modifier.weight(1f, true),
                    value = weekPosition.value,
                    onValueChange = { weekPosition.value = it },
                    valueRange = 0f..size.toFloat(),
                    steps = size - 1
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(id = R.string.add_week_after))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    val loadingFinished = remember { mutableStateOf(true) }
                    IconButton(onClick = {
                        loadingFinished.value = false
                        addWeekAfter { loadingFinished.value = true }
                    }, enabled = loadingFinished.value) {
                        AnimatedVisibility(visible = loadingFinished.value) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                stringResource(id = R.string.add_week_after),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        AnimatedVisibility(visible = !loadingFinished.value) {
                            CircularProgressIndicator(
                                Modifier
                                    .size(20.dp, 20.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
            Slider(value = sliderValue, onValueChange = {
                sliderValue = it
            }, onValueChangeFinished = {
                coroutineScope.launch {
                    dayPosition.animateScrollToPage(sliderValue.roundToInt())
                }
            }, valueRange = 0f..6f, steps = 5
            )
            Text(
                stringResource(id = R.string.days), style = MaterialTheme.typography.labelLarge
            )
        }
    }
}