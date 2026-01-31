package org.bxkr.octodiary.components


import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.R

@Composable
fun RankingMemberCard(
    rankPlace: Int,
    average: Double,
    memberName: String,
    highlighted: Boolean,
    isAnonymized: Boolean,
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
                var nameShown by remember { mutableStateOf(!isAnonymized) }
                var nameCopied by remember { mutableStateOf(false) }
                val clipboardManager = LocalClipboardManager.current
                AnimatedVisibility(!nameShown) {
                    Row(
                        Modifier
                            .padding(end = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.show_student_id),
                            Modifier
                                .clickable {
                                    nameShown = !nameShown
                                    if (!nameCopied) {
                                        clipboardManager.setText(AnnotatedString(memberName))
                                        nameCopied = true
                                    }
                                },
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                AnimatedVisibility(nameShown) {
                    Text(
                        memberName,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .fillMaxWidth()
                            .clickable {
                                if (isAnonymized) nameShown = !nameShown
                            }
                    )
                }
            }
        }
    }
}


