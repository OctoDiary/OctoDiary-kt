package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.CloverShape
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.MarkComp
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.events.Mark
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem

@Composable
fun SubjectCard(subject: MarkListSubjectItem) {
    val isGlow = subject.id == scrollToSubjectIdLive.value
    AnimatedContent(targetState = isGlow) { isGlowA ->
        Card(
            Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = CardDefaults.outlinedCardBorder().copy(
                3.dp,
                Brush.linearGradient(
                    0f to MaterialTheme.colorScheme.primary,
                    1f to MaterialTheme.colorScheme.tertiary
                )
            ).takeIf { isGlowA }
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        subject.subjectName,
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row {
                        if (subject.average != null) {
                            FilterChip(
                                onClick = {},
                                label = {
                                    Text(
                                        subject.average, color = when (subject.dynamic) {
                                            "UP" -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                                        }
                                    )
                                },
                                selected = true,
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if (subject.dynamic == "UP") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer),
                                leadingIcon = {
                                    if (subject.dynamic == "UP") {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDropUp,
                                            contentDescription = subject.dynamic,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDropDown,
                                            contentDescription = subject.dynamic,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                },
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                        if (subject.fixedValue != null) {
                            FilterChip(
                                selected = true, onClick = {}, label = { Text(subject.fixedValue) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Done,
                                        contentDescription = stringResource(id = R.string.final_mark),
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                },
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                if (subject.marks != null) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        LazyRow {
                            items(subject.marks) {
                                MarkComp(Mark.fromMarkListSubject(it), subjectId = subject.id)
                            }
                        }
                        if (LocalContext.current.mainPrefs.get("subject_rating") ?: true) {
                            DataService.subjectRanking.firstOrNull { it.subjectId == subject.id }
                                ?.let {
                                    FilledIconButton(onClick = {
                                        modalBottomSheetStateLive.postValue(true)
                                        modalBottomSheetContentLive.postValue {
                                            SubjectRatingBottomSheet(
                                                subject.id,
                                                subject.subjectName
                                            )
                                        }
                                    }, shape = CloverShape) {

                                        Text(
                                            it.rank.rankPlace.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}