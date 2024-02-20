package org.bxkr.octodiary.screens.navsections.dashboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.parseFromDay

@Composable
fun VisitsList() {
    LazyColumn(
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        items(DataService.visits.payload) {
            // MES sets "-" for visit time if there was no visit on that day
            if (it.visits[0].run { inX != "-" && out != "-" }) {
                OutlinedCard(Modifier.padding(bottom = 8.dp)) {

                    it.visits.forEachIndexed { index, visit ->
                        Row(Modifier.padding(8.dp)) {
                            Text(
                                it.date.parseFromDay().formatToHumanDay(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .alpha(
                                        if (index == 0) 1f else 0f
                                    )
                            )
                            Text(
                                visit.inX,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                stringResource(id = R.string.to),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                visit.out,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .fillMaxWidth()
                            )
                        }

                    }
                }
            }
        }
    }
}