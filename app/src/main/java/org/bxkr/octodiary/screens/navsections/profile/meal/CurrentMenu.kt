package org.bxkr.octodiary.screens.navsections.profile.meal


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.models.mealsmenucomplexes.Item
import org.bxkr.octodiary.models.mealsmenucomplexes.MealsMenuComplexes
import java.util.Date
import kotlin.math.roundToInt

private val complexesLive = MutableLiveData(DataService.mealsMenuComplexes)
private val dateLive = MutableLiveData<Date?>(null)
private val loadedLive = MutableLiveData(false)

@Composable
fun CurrentMenu(onMenuItemClick: (item: Item) -> Unit) {
    val date by dateLive.observeAsState(null)
    val loaded by loadedLive.observeAsState(false)
    val complexes by complexesLive.observeAsState(DataService.mealsMenuComplexes)
    var chooserShown by remember { mutableStateOf(false) }
    val isDemo = LocalContext.current.isDemo
    LaunchedEffect(date) {
        if (date != null && !isDemo) {
            loadedLive.value = false
            DataService.getMealsMenuComplexes(date ?: Date()) {
                complexesLive.value = it
                loadedLive.value = true
            }
        } else if (isDemo) loadedLive.value = true
    }
    Box {
        AnimatedVisibility(chooserShown) {
            DayChooser({ chooserShown = false }) {
                chooserShown = false
                dateLive.value = it
            }
        }
        AnimatedVisibility((date == null) || loaded) {
            complexes.MenuItems(date, onMenuItemClick) { chooserShown = true }
        }
        AnimatedVisibility((date != null) && !loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun MealsMenuComplexes.MenuItems(
    date: Date?,
    onMenuItemClick: (item: Item) -> Unit,
    chooserTrigger: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            item {
                Row(
                    Modifier
                        .padding(top = 8.dp, bottom = 16.dp, start = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(
                            R.string.menu_for_t,
                            date?.formatToHumanDay() ?: stringResource(R.string.today).lowercase()
                        )
                    )
                    OutlinedButton(
                        { chooserTrigger() },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            null,
                            Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.select_date))
                    }
                }
            }
            if (items != null) items(items) {
                ElevatedCardWithContent(onClick = { onMenuItemClick(it) }, title = {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayChooser(onDismissRequest: () -> Unit, setDate: (Date) -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(onDismissRequest, confirmButton = {
        TextButton(onClick = {
            val selectedDate = datePickerState.selectedDateMillis
            if (selectedDate != null) {
                setDate(Date().apply { time = selectedDate })
            }
        }) {
            Text(text = stringResource(id = R.string.select))
        }
    }) {
        DatePicker(state = datePickerState)
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
            IconButton(onClick = onClick, Modifier.padding(8.dp)) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back)
                )
            }
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        screen()
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
            Box(Modifier.padding(horizontal = 16.dp)) {
                ElevatedCardWithContent(
                    onClick = {
                        itemExpanded = !itemExpanded
                        itemItemRotation += 180f
                    },
                    title = {
                        Text(
                            dish.name,
                            Modifier
                                .padding(end = 8.dp)
                                .animateContentSize(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = if (!itemExpanded) 1 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis
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
                                    value = stringResource(R.string.weight_grams, dish.fat)
                                )
                            }
                            if (dish.carbohydrates != 0) {
                                MenuItemInfoValue(
                                    name = stringResource(R.string.carbohydrates),
                                    value = stringResource(
                                        R.string.weight_grams,
                                        dish.carbohydrates
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun MenuItemInfoValue(name: String, value: String) {
    Row {
        Text(
            name, modifier = Modifier
                .padding(end = 3.dp)
                .alpha(0.8f)
        )
        Text(value)
    }
}


