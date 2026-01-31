package org.bxkr.octodiary.screens.navsections.profile


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.MarkSimple
import org.bxkr.octodiary.formatToLongHumanDate
import org.bxkr.octodiary.formatToLongHumanDateNoTime
import org.bxkr.octodiary.getMarkConfig
import org.bxkr.octodiary.models.govexams.Exam
import org.bxkr.octodiary.parseFromDay
import java.util.Date

private val enterTransition1 = slideInHorizontally(
    tween(200)
) { it }
private val exitTransition1 = slideOutHorizontally(
    tween(200)
) { it }

private val enterTransition2 = slideInHorizontally(
    tween(200)
) { -it }

private val exitTransition2 = slideOutHorizontally(
    tween(200)
) { -it }


@Composable
fun ExamResults() {
    var showDetails by remember { mutableStateOf(false) }
    var detailsExam by remember { mutableStateOf<Exam?>(null) }

    Box(Modifier.animateContentSize()) {
        AnimatedVisibility(!showDetails, enter = enterTransition2, exit = exitTransition2) {
            ExamList {
                detailsExam = it
                showDetails = true
            }
        }
        AnimatedVisibility(showDetails, enter = enterTransition1, exit = exitTransition1) {
            ExamDetails(detailsExam!!) {
                showDetails = false
            }
        }
    }
}

@Composable
fun ExamList(onClickExam: (Exam) -> Unit) {
    val govExams = remember { DataService.govExams.data }
    val shownCategories = remember {
        mutableStateMapOf(*govExams.map {
            when (it.formaGia) {
                "ЕГЭ" -> Exam.ExamCategories.UnifiedStateExam
                "ОГЭ" -> Exam.ExamCategories.BasicStateExam
                else -> Exam.ExamCategories.Other
            }
        }.distinct().associateWith { true }.toList().toTypedArray()
        )
    }
    var showApprobation by remember { mutableStateOf(false) }

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(stringResource(R.string.exam_results), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.gia_title))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chip = @Composable { it: Map.Entry<Exam.ExamCategories, Boolean> ->
                FilterChip(it.value, { shownCategories[it.key] = !it.value }, {
                    Text(
                        stringResource(
                            when (it.key) {
                                Exam.ExamCategories.UnifiedStateExam -> R.string.ege
                                Exam.ExamCategories.BasicStateExam -> R.string.oge
                                Exam.ExamCategories.Other -> R.string.other
                            }
                        )
                    )
                }, leadingIcon = {
                    AnimatedVisibility(it.value) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(R.string.select),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                })
            }
            shownCategories.filter { it.key != Exam.ExamCategories.Other }.forEach {
                chip(it)
            }
            if (Exam.ExamCategories.Other in shownCategories) {
                chip(shownCategories.entries.first { it.key == Exam.ExamCategories.Other })
            }
            VerticalDivider(Modifier.height(40.dp))
            FilterChip(showApprobation, { showApprobation = !showApprobation }, {
                Text(stringResource(R.string.approbation))
            }, leadingIcon = {
                AnimatedVisibility(showApprobation) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(R.string.select),
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            })
        }

        LazyColumn {
            itemsIndexed(govExams) { index, exam ->
                AnimatedVisibility(
                    (shownCategories[exam.examCategory]
                        ?: false) && (!exam.approbation || showApprobation)
                ) {
                    val shape = if (index == 0) {
                        if (govExams.size == 1) MaterialTheme.shapes.large
                        else MaterialTheme.shapes.extraSmall.copy(
                            topStart = MaterialTheme.shapes.large.topStart,
                            topEnd = MaterialTheme.shapes.large.topEnd
                        )
                    } else if (index == govExams.lastIndex) MaterialTheme.shapes.extraSmall.copy(
                        bottomStart = MaterialTheme.shapes.large.bottomStart,
                        bottomEnd = MaterialTheme.shapes.large.bottomEnd
                    ) else MaterialTheme.shapes.extraSmall
                    Card(
                        Modifier
                            .padding(bottom = 2.dp)
                            .clip(shape)
                            .clickable { onClickExam(exam) },
                        shape = shape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(exam.name, style = MaterialTheme.typography.titleMedium)
                                Text("${exam.formaGia} ${if (exam.approbation) stringResource(R.string.approbation_suffix) else ""}")
                            }
                            MarkSimple(
                                if (exam.normalizedMarkBasis != null) exam.normalizedMarkValue else {
                                    if (exam.normalizedMarkValue == "1") "Зч" else "Нз"
                                }, getMarkConfig()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamDetails(exam: Exam, onDismiss: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 92.dp)
    ) {
        IconButton(onClick = onDismiss) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(id = R.string.back))
        }
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(exam.name, style = MaterialTheme.typography.titleMedium)
                Text("${exam.formaGia} ${if (exam.approbation) stringResource(R.string.approbation_suffix) else ""}")
                Text(exam.date.parseFromDay().formatToLongHumanDateNoTime())
            }
            MarkSimple(
                if (exam.normalizedMarkBasis != null) exam.normalizedMarkValue else {
                    if (exam.normalizedMarkValue == "1") "Зч" else "Нз"
                }, getMarkConfig()
            )
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.results), style = MaterialTheme.typography.titleMedium)
            Text(Date().apply { time = exam.timeStamp * 1000 }
                .formatToLongHumanDate(stringResource(R.string.at_time)))
            if (exam.primaryMarkBasis != 0)
                Text(
                    stringResource(
                        R.string.t_points_of_t,
                        exam.primaryMarkValue.toString(),
                        exam.primaryMarkBasis.toString()
                    )
                )
            else
                Text(stringResource(R.string.t_points, exam.primaryMarkValue.toString()))
            if (exam.examResult.variant != 0) Row {
                Text(
                    stringResource(R.string.variant),
                    Modifier
                        .padding(end = 4.dp)
                        .alpha(.8f)
                )
                Text(exam.examResult.variant.toString())
            }
            val partScore = @Composable { part: Int, value: Int ->
                Row {
                    Text(
                        stringResource(part),
                        Modifier
                            .padding(end = 4.dp)
                            .alpha(.8f)
                    )
                    Text(stringResource(R.string.t_points, value.toString()))
                }
            }
            exam.examResult.run {
                if (scoreA != 0) partScore(R.string.part_a, scoreA)
                if (scoreB != 0) partScore(R.string.part_b, scoreB)
                if (scoreC != 0) partScore(R.string.part_c, scoreC)
                if (scoreD != 0) partScore(R.string.part_d, scoreD)
            }
        }
    }
}


