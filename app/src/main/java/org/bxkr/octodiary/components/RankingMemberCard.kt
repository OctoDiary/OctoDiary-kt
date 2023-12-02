package org.bxkr.octodiary.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun RankingMemberCard(
    rankPlace: Int,
    average: Double,
    memberName: String,
    highlighted: Boolean
) {
    OutlinedCard(
        Modifier.padding(bottom = 8.dp),
        border = if (highlighted) BorderStroke(
            width = 2.dp,
            MaterialTheme.colorScheme.secondary
        ) else CardDefaults.outlinedCardBorder()
    ) {
        Row {
            Row(Modifier.padding(8.dp)) {
                Text(
                    rankPlace.toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    average.toString(),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    memberName,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}