package org.bxkr.octodiary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.events.Mark
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.parseSimpleLongAndFormatToLong

@Composable
fun Mark(
    mark: Mark,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (Mark) -> Unit = ::defaultMarkClick
) {
    FilledTonalIconButton(
        onClick = { onClick(mark) },
        modifier = modifier.clickable(enabled) { onClick(mark) },
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
fun MarkSheetContent(mark: Mark) {
    var markInfo by remember { mutableStateOf<MarkInfo?>(null) }
    LaunchedEffect(Unit) { DataService.getMarkInfo(mark.id) { markInfo = it } }

    Box(
        Modifier
            .heightIn(192.dp, Int.MAX_VALUE.dp)
            .fillMaxWidth()) {
        if (markInfo != null) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.mark), style = MaterialTheme.typography.titleMedium)
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
            Mark(mark,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp), false) {}
        } else {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

fun defaultMarkClick(mark: Mark) {
    modalBottomSheetStateLive.postValue(true)
    modalBottomSheetContentLive.postValue { MarkSheetContent(mark) }
}