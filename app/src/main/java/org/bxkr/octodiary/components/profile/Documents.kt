package org.bxkr.octodiary.components.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.persondata.Document


val enterTransition =
    expandVertically(
        expandFrom = Alignment.Top, animationSpec = tween(200)
    )

val exitTransition =
    shrinkVertically(
        shrinkTowards = Alignment.Top, animationSpec = tween(200)
    )


@Composable
fun Documents() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        with(DataService.personData) {
            Text(stringResource(R.string.documents), style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                item {
                    Row {
                        Text(stringResource(R.string.snils_t))
                        val snils = DataService.profile.children[DataService.currentProfile].snils
                        Text(snils, Modifier.clickable { copy(snils) })
                    }
                }
                items(documents) {
                    DocumentCard(document = it)
                }
            }
        }
    }
}

@Composable
fun DocumentCard(document: Document) {
    var isExpanded by remember { mutableStateOf(false) }
    var rotation by remember { mutableFloatStateOf(0f) }
    val rotationAnim by
    animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(600),
        label = "rotate_anim"
    )

    val clipboardManager = LocalClipboardManager.current
    val copy: (String) -> Unit = remember {
        { clipboardManager.setText(AnnotatedString(it)) }
    }

    Card(
        Modifier
            .padding(top = 16.dp)
            .clip(CardDefaults.shape)
            .clickable {
                isExpanded = !isExpanded
                rotation += 180f
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                document.documentType.name,
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                Icons.Rounded.ArrowDropDown,
                stringResource(R.string.expand),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .rotate(rotationAnim)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        AnimatedVisibility(
            isExpanded, exit = exitTransition, enter = enterTransition
        ) {
            Column(
                Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                document.displayData.forEach {
                    if (it.value != null) {
                        DocumentValue(stringResource(it.key), it.value!!) { copy(it.value!!) }
                    }
                }
            }
        }
    }
}


@Composable
fun DocumentValue(name: String, value: String, onClick: () -> Unit) {
    Row {
        Text(
            name, modifier = Modifier
                .padding(end = 3.dp)
                .alpha(0.8f)
        )
        Text(value, modifier = Modifier.clickable { onClick() })
    }
}