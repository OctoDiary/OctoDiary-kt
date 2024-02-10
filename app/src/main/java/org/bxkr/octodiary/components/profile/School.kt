package org.bxkr.octodiary.components.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Phone
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R

@Composable
fun School() {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        val context = LocalContext.current
        with(DataService.schoolInfo) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            if (null !in listOf(address.county, address.district, address.address)) {
                val address = address.run { "${county ?: ""} ${district ?: ""} ${address ?: ""}" }
                TextWithIcon(icon = Icons.Rounded.LocationCity) {
                    Text(
                        address,
                        Modifier.clickable {
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:55.76,37.62?q=$address")
                            ).let {
                                context.startActivity(it)
                            }
                        }
                    )
                }
            }
            if (phone != null) {
                TextWithIcon(icon = Icons.Rounded.Phone) {
                    Text(
                        "+7 $phone",
                        Modifier.clickable {
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:+7$phone")).let {
                                context.startActivity(it)
                            }
                        }
                    )
                }
            }
            if (email != null) {
                TextWithIcon(icon = Icons.Rounded.AlternateEmail) {
                    Text(
                        email,
                        Modifier.clickable {
                            Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$email")).let {
                                context.startActivity(it)
                            }
                        }
                    )
                }
            }
            if (websiteLink != null) {
                TextWithIcon(icon = Icons.Rounded.Language) {
                    Text(
                        websiteLink,
                        Modifier.clickable {
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://$websiteLink")).let {
                                context.startActivity(it)
                            }
                        },
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            var teachersExpanded by remember { mutableStateOf(false) }
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
                    teachersExpanded = !teachersExpanded
                    rotation += 180f
                }) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.teachers),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            Icons.Rounded.ArrowDropDown,
                            stringResource(R.string.expand),
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
                        )
                    }
                    AnimatedVisibility(
                        teachersExpanded, enter = enterTransition, exit = exitTransition
                    ) {
                        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                            items(teachers) {
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
                                        Column {
                                            it.subjectNames.forEach {
                                                Text(
                                                    it,
                                                    style = MaterialTheme.typography.labelMedium
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
        }
    }
}

@Composable
fun TextWithIcon(icon: ImageVector, text: @Composable () -> Unit) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = "", modifier = Modifier.padding(end = 8.dp))
        text.invoke()
    }
}