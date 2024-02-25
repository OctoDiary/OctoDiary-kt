package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.components.MarkComp
import org.bxkr.octodiary.components.defaultMarkClick
import org.bxkr.octodiary.models.marklistdate.Mark

@Composable
fun ExtendedMark(mark: Mark) {
    val eventMark = org.bxkr.octodiary.models.events.Mark.fromMarkListDate(mark)
    Column(Modifier.clickable { defaultMarkClick(eventMark, mark.subjectId) }) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(mark.subjectName, style = MaterialTheme.typography.titleMedium)
                Text(mark.controlFormName)
            }
            MarkComp(eventMark, subjectId = mark.subjectId)
        }
    }
}