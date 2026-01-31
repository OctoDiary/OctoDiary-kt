package org.bxkr.octodiary.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.R
import org.bxkr.octodiary.isPackageInstalled
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier
) {
    val diaries = remember { Diary.values() } // TODO: replace with .entries after updating to Kotlin 1.9.2
    val pagerState = rememberPagerState { diaries.size }
    val coroutineScope = rememberCoroutineScope()
    var alertTrigger by remember { mutableStateOf(false) }
    var alertAction by remember { mutableStateOf({}) }

    val setAlertTrigger = remember { { trigger: () -> Unit ->
        alertAction = trigger
        alertTrigger = true
    } }

    if (alertTrigger) {
        ShowAlertIfFoundReceivers {
            alertAction()
            alertTrigger = false
        }
    }

    Column(modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            diaries.forEach { diary ->
                Tab(
                    selected = pagerState.currentPage == diary.ordinal,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(diary.ordinal)
                        }
                    },
                    text = { Text(stringResource(id = diary.title)) },
                    icon = { Icon(diary.icon, stringResource(diary.title)) }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            val diary = diaries[page]
            Column(
                Modifier.fillMaxSize(),
                Arrangement.Center
            ) {
                PrimaryLogInButton(diary, setAlertTrigger)

                Divider(text = stringResource(id = R.string.or))

                diary.alternativeLogIn(
                    Modifier.align(Alignment.CenterHorizontally),
                    setAlertTrigger,
                )
            }
        }
    }
}


@Composable
fun ColumnScope.Divider(text: String) {
    Row(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier
                .width(96.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier
                .width(96.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ColumnScope.PrimaryLogInButton(diary: Diary, setAlertTrigger: (() -> Unit) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .width(TextFieldDefaults.MinWidth)
            .height(TextFieldDefaults.MinHeight)
            .align(Alignment.CenterHorizontally)
            .background(
                Brush.linearGradient(
                    diary.primaryLogGradientColors
                        .map { colorResource(it) }), MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                setAlertTrigger {
                    diary.primaryLogInFunction(context)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        DiaryIcon(diary)
        Text(
            stringResource(id = diary.primaryLogInLabel),
            color = Color.White
        )
    }}

@Composable
private fun DiaryIcon(diary: Diary) {
    when (diary) {
        Diary.MES -> {
            Image(
                painterResource(R.drawable.ic_mos_ru),
                stringResource(id = R.string.log_in),
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .width(16.dp)
            )
        }
        else -> {
            Icon(
                Icons.AutoMirrored.Default.OpenInNew,
                contentDescription = stringResource(id = R.string.log_in),
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun ShowAlertIfFoundReceivers(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var isShown by remember {
        mutableStateOf(
            listOf(
                "ru.mes.dnevnik",
                "ru.mes.dnevnik.fgis"
            ).any { context.packageManager.isPackageInstalled(it) }
        )
    }
    LaunchedEffect(Unit) {
        if (!isShown) onDismiss()
    }
    AnimatedVisibility(isShown) {
        AlertDialog(
            onDismissRequest = { isShown = false; onDismiss() },
            confirmButton = {
                TextButton(onClick = { isShown = false; onDismiss() }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.warning)) },
            text = {
                Text(
                    stringResource(
                        R.string.auth_receivers_warn, stringResource(R.string.app_name)
                    )
                )
            }
        )
    }
}

@Preview
@Composable
fun LoginPreview() {
    OctoDiaryTheme {
        Surface {
            LoginScreen()
        }
    }
}



