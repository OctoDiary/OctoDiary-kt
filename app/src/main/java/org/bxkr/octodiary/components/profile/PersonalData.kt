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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.formatToHumanDate
import org.bxkr.octodiary.parseFromDay

@Composable
fun PersonalData() {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        val clipboardManager = LocalClipboardManager.current
        with(DataService.profile.children[0]) {
            Text("$lastName $firstName $middleName", style = MaterialTheme.typography.titleMedium)
            Text(
                "Дата рождения: ${
                    birthDate.parseFromDay().formatToHumanDate()
                }"
            ) // FUTURE: UNTRANSLATED
            Row {
                Text("СНИЛС: ") // FUTURE: UNTRANSLATED
                var snilsShown by remember { mutableStateOf(false) }
                var snilsCopied by remember { mutableStateOf(false) }
                AnimatedVisibility(!snilsShown) {
                    Text("показать", Modifier.clickable {
                        if (!snilsCopied) {
                            clipboardManager.setText(AnnotatedString(snils))
                            snilsCopied = true
                        }
                        snilsShown = true
                    }, color = MaterialTheme.colorScheme.secondary) // FUTURE: UNTRANSLATED
                }
                AnimatedVisibility(snilsShown) {
                    Text(snils, Modifier.clickable {
                        snilsShown = false
                    })
                }
            }


            var representativesExpanded by remember { mutableStateOf(false) }
            var rotation by remember { mutableFloatStateOf(0f) }
            val enterTransition = remember {
                expandVertically(
                    expandFrom = Alignment.Top, animationSpec = tween(200)
                )
            }
            val exitTransition = remember {
                shrinkVertically(
                    shrinkTowards = Alignment.Top, animationSpec = tween(200)
                )
            }
            ElevatedCard(Modifier.padding(top = 16.dp)) {
                Column(Modifier.clickable {
                    representativesExpanded = !representativesExpanded
                    rotation += 180f
                }) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Представители", style = MaterialTheme.typography.titleMedium
                        ) // FUTURE: UNTRANSLATED
                        Icon(
                            Icons.Rounded.ArrowDropDown,
                            "Развернуть",
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .rotate(
                                    animateFloatAsState(
                                        targetValue = rotation,
                                        animationSpec = tween(600),
                                        label = "rotate_anim"
                                    ).value
                                )
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        ) // FUTURE: UNTRANSLATED
                    }
                    AnimatedVisibility(
                        representativesExpanded, enter = enterTransition, exit = exitTransition
                    ) {
                        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                            items(representatives) {
                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                            1.dp
                                        )
                                    )
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "${it.lastName} ${it.firstName} ${it.middleName}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("+7 ${it.phone}", Modifier.clickable {
                                            clipboardManager.setText(AnnotatedString("+7${it.phone}"))
                                        }) // FUTURE: UNTRANSLATED
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}