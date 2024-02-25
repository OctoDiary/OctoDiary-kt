package org.bxkr.octodiary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.CloverShape
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.NavSection
import org.bxkr.octodiary.R
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.events.Mark
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.navControllerLive
import org.bxkr.octodiary.parseSimpleLongAndFormatToLong
import org.bxkr.octodiary.screens.navsections.marks.SubjectRatingBottomSheet
import org.bxkr.octodiary.screens.navsections.marks.scrollToSubjectIdLive

@Composable
fun MarkComp(
    mark: Mark,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subjectId: Long,
    onClick: (Mark, Long) -> Unit = ::defaultMarkClick,
) {
    FilledTonalIconButton(
        onClick = { onClick(mark, subjectId) },
        modifier = modifier.clickable(enabled) { onClick(mark, subjectId) },
        shape = MaterialTheme.shapes.small
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            Text(
                mark.value,
                Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                mark.weight.toString(),
                Modifier.align(Alignment.BottomEnd),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun MarkSheetContent(mark: Mark, subjectId: Long) {
    var markInfo by remember { mutableStateOf<MarkInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        DataService.getMarkInfo(mark.id, { errorMessage = it }) {
            markInfo = it
        }
    }
    val subject = remember {
        if (DataService.hasMarksSubject) {
            DataService.marksSubject.firstOrNull { it.id == subjectId }
        } else null
    }

    Box(
        Modifier
            .heightIn(192.dp, Int.MAX_VALUE.dp)
            .fillMaxWidth()
    ) {
        if (markInfo != null) {
            Column(Modifier.padding(16.dp)) {
                if (subject != null) {
                    Text(
                        subject.subjectName,
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        stringResource(R.string.mark),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                with(markInfo!!) {
                    Text(teacher.run { "$lastName $firstName $middleName" })
                    Text(controlFormName)
                    if (commentExists) Text(comment!!)
                    Text(
                        stringResource(
                            R.string.mark_created,
                            parseSimpleLongAndFormatToLong(
                                updatedAt,
                                stringResource(id = R.string.at_time)
                            )
                        ),
                        Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            val context = LocalContext.current
            Column(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MarkComp(mark, enabled = false, subjectId = 0L)
                if (subject != null) {
                    if (context.mainPrefs.get("subject_rating") ?: true) {
                        DataService.subjectRanking.firstOrNull { it.subjectId == subject.id }?.let {
                            FilledIconButton(
                                {
                                    modalBottomSheetContentLive.postValue {
                                        SubjectRatingBottomSheet(
                                            subject.id,
                                            subject.subjectName
                                        )
                                    }
                                },
                                Modifier.padding(top = 6.dp),
                                shape = CloverShape
                            ) {
                                Text(
                                    it.rank.rankPlace.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    val navController = navControllerLive.observeAsState().value
                    Row(
                        Modifier
                            .padding(top = 8.dp)
                            .clip(CircleShape)
                            .let {
                                if (navController != null) {
                                    it.clickable {
                                        modalBottomSheetStateLive.postValue(false)
                                        scrollToSubjectIdLive.value = subjectId
                                        navController.navigate(route = NavSection.Marks.route)
                                    }
                                } else it
                            }
                            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val textStyle = MaterialTheme.typography.labelLarge
                        Icon(
                            Icons.AutoMirrored.Rounded.TrendingUp,
                            stringResource(R.string.average_mark),
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(textStyle.fontSize.value.dp),
                            MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            subject.average ?: subject.fixedValue ?: "",
                            Modifier.padding(end = 4.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            style = textStyle
                        )
                    }
                }
            }
        } else if (errorMessage == null) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            ErrorMessage(Modifier.align(Alignment.Center), errorMessage!!)
        }
    }
}

fun defaultMarkClick(mark: Mark, subjectId: Long) {
    modalBottomSheetStateLive.postValue(true)
    modalBottomSheetContentLive.postValue { MarkSheetContent(mark, subjectId) }
}