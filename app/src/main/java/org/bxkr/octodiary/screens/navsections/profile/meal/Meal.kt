package org.bxkr.octodiary.screens.navsections.profile.meal


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.bxkr.octodiary.R
import org.bxkr.octodiary.modalDialogStateLive
import org.bxkr.octodiary.ui.theme.enterTransition
import org.bxkr.octodiary.ui.theme.enterTransition1
import org.bxkr.octodiary.ui.theme.enterTransition2
import org.bxkr.octodiary.ui.theme.exitTransition
import org.bxkr.octodiary.ui.theme.exitTransition1
import org.bxkr.octodiary.ui.theme.exitTransition2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDialog() {
    val onDismiss = { modalDialogStateLive.postValue(false) }
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onDismiss
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.meal)) },
                    navigationIcon = {
                        IconButton(onDismiss) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        ) { contentPadding ->
            Column(Modifier.padding(contentPadding)) {
                Meal()
            }
        }
    }
}

enum class MealScreens {
    CurrentMenu,
    DiningHistory
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Meal() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        var isMainScreen by remember { mutableStateOf(true) }
        var currentMenuItemScreen by remember { mutableStateOf<@Composable () -> Unit>({}) }
        var currentTitle by remember { mutableStateOf("") }
        var currentScreen by remember { mutableStateOf(MealScreens.CurrentMenu) }


        PrimaryTabRow(currentScreen.ordinal) {
            Tab(
                selected = currentScreen == MealScreens.CurrentMenu,
                onClick = { currentScreen = MealScreens.CurrentMenu },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                text = { Text(stringResource(R.string.meal_dining_menu)) },
                icon = { Icon(Icons.Default.SetMeal, null) }
            )
            Tab(
                selected = currentScreen == MealScreens.DiningHistory,
                onClick = { currentScreen = MealScreens.DiningHistory },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                text = { Text(stringResource(R.string.food_history)) },
                icon = { Icon(Icons.Default.History, null) }
            )
        }
        Box {
            Column {
                AnimatedVisibility(
                    isMainScreen, enter = enterTransition2, exit = exitTransition2
                ) {
                    Crossfade(
                        currentScreen,
                        label = "meal_screens_anim"
                    ) { screen ->
                        when (screen) {
                            MealScreens.CurrentMenu -> CurrentMenu {
                                isMainScreen = false
                                currentTitle = it.name + " - " + it.humanPrice
                                currentMenuItemScreen = {
                                    MenuItem(
                                        it, enterTransition, exitTransition
                                    )
                                }
                            }

                            MealScreens.DiningHistory -> DiningHistory()
                        }
                    }
                }
            }
            Column {
                AnimatedVisibility(
                    visible = !isMainScreen,
                    enter = enterTransition1,
                    exit = exitTransition1
                ) {
                    MenuItemLayout(title = currentTitle, screen = currentMenuItemScreen) {
                        isMainScreen = true
                        currentMenuItemScreen = {}
                        currentTitle = ""
                    }
                }
            }
        }
    }
}


