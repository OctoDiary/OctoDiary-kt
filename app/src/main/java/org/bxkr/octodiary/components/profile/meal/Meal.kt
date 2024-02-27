package org.bxkr.octodiary.components.profile.meal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToLongHumanDay
import org.bxkr.octodiary.formatToWeekday
import org.bxkr.octodiary.models.daysbalanceinfo.Day
import org.bxkr.octodiary.models.mealsmenucomplexes.Item
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.ui.theme.enterTransition
import org.bxkr.octodiary.ui.theme.enterTransition1
import org.bxkr.octodiary.ui.theme.enterTransition2
import org.bxkr.octodiary.ui.theme.exitTransition
import org.bxkr.octodiary.ui.theme.exitTransition1
import org.bxkr.octodiary.ui.theme.exitTransition2
import kotlin.math.roundToInt

@Composable
fun Meal() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        var isMainScreen by remember { mutableStateOf(true) }
        var currentMenuItemScreen by remember { mutableStateOf<@Composable () -> Unit>({}) }
        var currentTitle by remember { mutableStateOf("") }

        AnimatedVisibility(
            visible = !isMainScreen, enter = enterTransition1, exit = exitTransition1
        ) {
            MenuItemLayout(title = currentTitle, screen = currentMenuItemScreen) {
                isMainScreen = true
                currentMenuItemScreen = {}
                currentTitle = ""
            }
        }

        AnimatedVisibility(
            isMainScreen, enter = enterTransition2, exit = exitTransition2
        ) {
            Column {
                Box(
                    Modifier
                        .padding(bottom = 32.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.meal),
                        Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Icon(
                        Icons.Rounded.Fastfood,
                        stringResource(R.string.meal),
                        Modifier
                            .align(Alignment.Center)
                            .size(92.dp)
                            .alpha(0.1f)
                    )
                }

                with(DataService.mealsMenuComplexes) {
                    var menuExpanded by remember { mutableStateOf(false) }
                    var rotation by remember { mutableFloatStateOf(0f) }

                    ElevatedCardWithContent(onClick = {
                        menuExpanded = !menuExpanded
                        rotation += 180f
                    }, title = {
                        Text(
                            stringResource(R.string.meal_dining_menu),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }, rotation = rotation) {
                        AnimatedVisibility(
                            menuExpanded, enter = enterTransition, exit = exitTransition
                        ) {
                            Column(
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                            ) {
                                var dayExpanded by remember { mutableStateOf(false) }
                                var dayRotation by remember { mutableFloatStateOf(0f) }

                                ElevatedCardWithContent(onClick = {
                                    dayExpanded = !dayExpanded
                                    dayRotation += 180f
                                }, title = {
                                    Text(
                                        stringResource(R.string.today),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }, rotation = dayRotation) {
                                    AnimatedVisibility(
                                        dayExpanded, enter = enterTransition, exit = exitTransition
                                    ) {
                                        LazyColumn(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                .fillMaxWidth()
                                        ) {
                                            items(items) {
                                                ElevatedCardWithContent(onClick = {
                                                    isMainScreen = false
                                                    currentTitle = it.name + " - " + it.humanPrice
                                                    currentMenuItemScreen = {
                                                        MenuItem(
                                                            it, enterTransition, exitTransition
                                                        )
                                                    }
                                                }, title = {
                                                    Text(it.name)
                                                    Text(
                                                        it.humanPrice,
                                                        Modifier
                                                            .padding(start = 8.dp)
                                                            .alpha(.8f)
                                                    )
                                                }, rotation = 270f
                                                ) {}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                with(DataService.daysBalanceInfo) {
                    var daysExpanded by remember { mutableStateOf(false) }
                    var rotation by remember { mutableFloatStateOf(0f) }
                    var countExpandedDays by remember { mutableIntStateOf(10) }

                    ElevatedCardWithContent(onClick = {
                        daysExpanded = !daysExpanded
                        rotation += 180f
                    }, title = {
                        Text(
                            stringResource(R.string.food_history),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }, rotation = rotation) {
                        AnimatedVisibility(
                            daysExpanded, enter = enterTransition, exit = exitTransition
                        ) {
                            LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                                items(days) { day ->
                                    val visible = days.indexOf(day) + 1 <= countExpandedDays
                                    var expanded by remember(key1 = day.date) { mutableStateOf(false) }
                                    var dayRotation by remember(key1 = day.date) {
                                        mutableFloatStateOf(
                                            0f
                                        )
                                    }


                                    AnimatedVisibility(
                                        visible, enter = enterTransition, exit = exitTransition
                                    ) {
                                        ElevatedCardWithContent(onClick = {
                                            expanded = !expanded
                                            dayRotation += 180f
                                        }, title = {
                                            Text(
                                                day.date.parseFromDay()
                                                    .formatToLongHumanDay(includeYear = true),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                day.date.parseFromDay().formatToWeekday(),
                                                Modifier
                                                    .padding(start = 8.dp)
                                                    .alpha(.8f),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }, icons = {
                                            FoodDayIndicators(day)
                                        }, rotation = dayRotation
                                        ) {
                                            AnimatedVisibility(
                                                expanded,
                                                enter = enterTransition,
                                                exit = exitTransition
                                            ) {
                                                Column(
                                                    Modifier
                                                        .padding(
                                                            horizontal = 16.dp, vertical = 4.dp
                                                        )
                                                        .fillMaxWidth()
                                                ) {
                                                    day.transactions.forEach { transaction ->
                                                        var transactionsExpanded by remember(key1 = transaction.type + transaction.sum + day.date) {
                                                            mutableStateOf(false)
                                                        }
                                                        var transactionsRotation by remember(key1 = transaction.type + transaction.sum + day.date) {
                                                            mutableFloatStateOf(0f)
                                                        }

                                                        ElevatedCardWithContent(onClick = {
                                                            transactionsExpanded =
                                                                !transactionsExpanded
                                                            transactionsRotation += 180f
                                                        }, title = {
                                                            val title = when (transaction.type) {
                                                                "DINING" -> stringResource(
                                                                    id = R.string.dining
                                                                )

                                                                "BUFFET" -> stringResource(
                                                                    id = R.string.buffet
                                                                )

                                                                else -> transaction.type
                                                            }

                                                            Text(
                                                                title,
                                                                style = MaterialTheme.typography.titleMedium
                                                            )
                                                            Text(
                                                                (transaction.sum.toFloat() / 100.toFloat()).roundToInt()
                                                                    .toString() + " ₽",
                                                                Modifier
                                                                    .padding(start = 8.dp)
                                                                    .alpha(.8f)
                                                            )
                                                        }, rotation = transactionsRotation
                                                        ) {
                                                            AnimatedVisibility(
                                                                transactionsExpanded,
                                                                enter = enterTransition,
                                                                exit = exitTransition
                                                            ) {
                                                                Column(
                                                                    Modifier
                                                                        .padding(16.dp)
                                                                        .fillMaxWidth()
                                                                ) {
                                                                    transaction.items.forEach { item ->
                                                                        if (item.dishes.isNotEmpty()) {
                                                                            item.dishes.forEach { dish ->
                                                                                Row(
                                                                                    Modifier
                                                                                        .padding(
                                                                                            vertical = 4.dp
                                                                                        )
                                                                                        .fillMaxWidth(),
                                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                                ) {
                                                                                    Text(
                                                                                        dish.title,
                                                                                        Modifier
                                                                                            .weight(
                                                                                                1f,
                                                                                                false
                                                                                            )
                                                                                            .animateContentSize(),
                                                                                        color = MaterialTheme.colorScheme.tertiary
                                                                                    )

                                                                                    if (dish.price != null && dish.price > 0) {
                                                                                        Text(
                                                                                            (dish.price / 100.00).toString() + " ₽",
                                                                                            modifier = Modifier
                                                                                                .padding(
                                                                                                    start = 8.dp
                                                                                                )
                                                                                                .alpha(
                                                                                                    .8f
                                                                                                ),
                                                                                        )
                                                                                    }

                                                                                    if (dish.amount != null && dish.amount > 1) {
                                                                                        Text(
                                                                                            dish.amount.toString() + "x",
                                                                                            modifier = Modifier
                                                                                                .padding(
                                                                                                    start = 8.dp
                                                                                                )
                                                                                                .alpha(
                                                                                                    .8f
                                                                                                ),
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
                                    }
                                }
                                item {
                                    val visible = countExpandedDays < days.size

                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = enterTransition,
                                        exit = exitTransition
                                    ) {
                                        Box(
                                            Modifier.padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    countExpandedDays += 10
                                                }, modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    stringResource(R.string.show_more),
                                                    color = MaterialTheme.colorScheme.tertiary
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
fun MenuItemLayout(title: String, screen: @Composable () -> Unit, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back)
                )
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        screen()
    }
}


@Composable
fun ElevatedCardWithContent(
    onClick: () -> Unit,
    title: @Composable RowScope.() -> Unit,
    icons: @Composable RowScope.() -> Unit = {},
    rotation: Float = 0f,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        Modifier.padding(bottom = 16.dp),
    ) {
        Column(Modifier.clickable { onClick() }) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .weight(1f, false)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    title()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    icons()
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

            }
            content()
        }
    }
}

@Composable
fun MenuItem(
    mealUnit: Item,
    enterTransition: EnterTransition,
    exitTransition: ExitTransition,
) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        mealUnit.items.forEach { dish ->
            var itemExpanded by remember(key1 = dish.id) { mutableStateOf(false) }
            var itemItemRotation by remember(key1 = dish.id) { mutableFloatStateOf(0f) }
            ElevatedCardWithContent(
                onClick = {
                    itemExpanded = !itemExpanded
                    itemItemRotation += 180f
                },
                title = {
                    Text(
                        dish.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = if (!itemExpanded) 1 else Int.MAX_VALUE
                    )
                },
                rotation = itemItemRotation
            ) {
                AnimatedVisibility(
                    itemExpanded, enter = enterTransition, exit = exitTransition
                ) {
                    Column(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (dish.price != 0) {
                            MenuItemInfoValue(
                                name = stringResource(R.string.price),
                                value = "${(dish.price.toFloat() / 100.00).roundToInt()} ₽"
                            )
                        }
                        if (dish.ingredients.isNotEmpty()) {
                            MenuItemInfoValue(
                                name = stringResource(R.string.ingredients),
                                value = dish.ingredients
                            )
                        }
                        if (dish.calories != 0) {
                            MenuItemInfoValue(
                                name = stringResource(R.string.calories),
                                value = stringResource(R.string.energy_value, dish.calories)
                            )
                        }
                        if (dish.protein != 0) {
                            MenuItemInfoValue(
                                name = stringResource(R.string.proteins),
                                value = stringResource(R.string.weight_grams, dish.protein)
                            )
                        }
                        if (dish.fat != 0) {
                            MenuItemInfoValue(
                                name = stringResource(R.string.fats),
                                value = stringResource(R.string.energy_value, dish.fat)
                            )
                        }
                        if (dish.carbohydrates != 0) {
                            MenuItemInfoValue(
                                name = stringResource(R.string.carbohydrates),
                                value = stringResource(R.string.energy_value, dish.carbohydrates)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MenuItemInfoValue(name: String, value: String) {
    Row {
        Text(
            name, modifier = Modifier
                .padding(end = 3.dp)
                .alpha(0.8f)
        )
        Text(value)
    }
}

@Composable
fun FoodDayIndicators(day: Day) {
    org.bxkr.octodiary.components.profile.FoodDayIndicators.values().forEach {
        if (it.condition(day)) {
            Icon(
                it.icon,
                stringResource(it.descriptionRes),
                Modifier.padding(end = 4.dp),
                MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}