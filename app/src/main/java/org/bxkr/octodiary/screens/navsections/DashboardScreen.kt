package org.bxkr.octodiary.screens.navsections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R

@Composable
fun DashboardScreen() {
    Column(verticalArrangement = Arrangement.Bottom) {
        OutlinedCard(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                Text(
                    stringResource(
                        id = R.string.rating_place,
                        DataService
                            .run { ranking.first { it.personId == sessionUser.personId } }
                            .rank.rankPlace
                    )
                )
            }
        }
    }
}