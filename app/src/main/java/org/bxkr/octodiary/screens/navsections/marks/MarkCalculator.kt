package org.bxkr.octodiary.screens.navsections.marks


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.MarkComp
import org.bxkr.octodiary.components.MarkConfig
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.models.events.Mark.MarkCompanion.fromMarkListSubject
import org.bxkr.octodiary.models.marklistsubject.Mark
import org.bxkr.octodiary.models.marklistsubject.Period
import org.bxkr.octodiary.save
import org.bxkr.octodiary.simpleMark
import org.bxkr.octodiary.times
import kotlin.math.round
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MarkCalculator(
    period: Period,
    subjectId: Long,
    subjectName: String,
    markConfig: MarkConfig,
) {
    var textValue by remember { mutableStateOf("") }
    var marks by remember { mutableStateOf(period.marks) }
    var marksAll by remember { mutableStateOf(period.marks) }
    val mainPrefs = LocalContext.current.mainPrefs
    LaunchedEffect(Unit) { mainPrefs.save("show_calc_hint" to false) }
    var choice by remember { mutableStateOf(mainPrefs.get<String>("calculator_mode") ?: "buttons") }
    Box(Modifier.fillMaxWidth()) {
        Helper(choice, Modifier.align(Alignment.TopEnd))
        Column(Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.mark_calculator),
                style = MaterialTheme.typography.titleMedium
            )
            Text("$subjectName, ${period.title.lowercase()}")
            AnimatedVisibility(marks.isValid() && marks.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    AverageChip(marks.calculate().toString(), period.dynamic) {}
                    FinalChip(marks.calculate().roundToInt().toString())
                }
            }
            AnimatedVisibility(!marks.isValid()) {
                Text(
                    stringResource(R.string.remove_non_numeric_marks),
                    color = MaterialTheme.colorScheme.error
                )
            }
            FlowRow {
                marksAll.forEach { mark ->
                    AnimatedVisibility(mark.id in marks.map { it.id }) {
                        MarkComp(
                            fromMarkListSubject(mark),
                            subjectId = subjectId,
                            markConfig = markConfig
                        ) { _, _ ->
                            marks = marks.filter { it.id != mark.id }
                        }
                    }
                }
            }
            mainPrefs.save("calculator_mode" to choice)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    choice == "buttons",
                    { choice = "buttons" },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text(stringResource(R.string.buttons)) }
                SegmentedButton(
                    choice == "keyboard",
                    { choice = "keyboard" },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text(stringResource(R.string.keyboard)) }
            }
            AnimatedContent(choice, label = "mode_anim") { mode ->
                when (mode) {
                    "buttons" -> {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(5, 4, 3, 2).forEach {
                                MarkComp(
                                    fromMarkListSubject(simpleMark(it)),
                                    subjectId = subjectId,
                                    markConfig = markConfig,
                                    showPlus = true
                                ) { _, _ ->
                                    val newMark = listOf(simpleMark(it))
                                    marks += newMark
                                    marksAll += newMark
                                }
                            }
                        }
                    }

                    "keyboard" -> OutlinedTextField(
                        textValue,
                        { textValue = it },
                        Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.add_mark)) },
                        trailingIcon = {
                            IconButton({
                                val newMarks = markFactory(textValue.replace("\\s+", " "))
                                marks += newMarks
                                marksAll += newMarks
                                textValue = ""
                            }) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    stringResource(R.string.add)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Helper(choice: String, modifier: Modifier = Modifier) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val coroutineScope = rememberCoroutineScope()
    Box(modifier.padding(8.dp)) {
        TooltipBox(
            TooltipDefaults.rememberPlainTooltipPositionProvider(),
            {
                PlainTooltip {
                    Text(
                        stringResource(if (choice == "keyboard") R.string.add_mark_help_keyboard else R.string.add_mark_help_buttons)
                    )
                }
            },
            tooltipState
        ) {
            IconButton({
                coroutineScope.launch { tooltipState.show() }
            }) {
                Icon(
                    Icons.AutoMirrored.Default.HelpOutline,
                    stringResource(R.string.help),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun List<Mark>.isValid(): Boolean = !map { it.value.toIntOrNull() }.any { it == null }

private fun List<Mark>.calculate(): Double =
    if (isValid() && isNotEmpty()) {
        map { listOf(it.value.toInt()) * it.weight }.flatten().let { it.sum().toDouble() / it.size }
            .let { round(it * 100) / 100 }
    } else 0.0

private fun markFactory(string: String): List<Mark> {
    val mutableList: MutableList<Mark> = mutableListOf()
    string.split(' ').forEach { mark ->
        if ("^" !in mark && (mark.toIntOrNull() != null)) {
            mutableList.add(simpleMark(mark.toInt()))
        }
        if ("^" in mark) {
            val components = mark.split('^')
            if (components.size == 2 && components.map { it.toIntOrNull() }.any { it == null }
                    .not()) {
                mutableList.add(simpleMark(components[0].toInt(), components[1].toInt()))
            }
        }
    }
    return mutableList.toList()
}


