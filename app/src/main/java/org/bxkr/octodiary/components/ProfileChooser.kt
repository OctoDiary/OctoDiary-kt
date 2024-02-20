package org.bxkr.octodiary.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToHumanDate
import org.bxkr.octodiary.modalDialogStateLive
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import org.bxkr.octodiary.parseFromDay


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileChooser() {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
        Text(
            stringResource(id = R.string.choose_context_profile),
            Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        val context = LocalContext.current
        DataService.profile.children.forEachIndexed { index, it ->
            OutlinedCard(
                Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .clip(CardDefaults.outlinedShape)
                    .clickable {
                        modalDialogStateLive.postValue(false)
                        DataService.currentProfile = index
                        DataService.loadedEverything.value = false
                        DataService.loadingStarted = false
                        DataService.updateAll(context)
                    },
                border = if (DataService.currentProfile == index) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlideImage(
                        model = SecondaryAPI.getAvatarUrl(DataService.subsystem, it.contingentGuid),
                        contentDescription = stringResource(id = R.string.image),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(CircleShape)
                    )
                    Column {
                        Text(
                            it.run { "$firstName $lastName" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(it.birthDate.parseFromDay().formatToHumanDate())
                    }
                }
            }
        }
    }
}