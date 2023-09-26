package org.bxkr.octodiary.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    var currentPage by rememberSaveable { mutableStateOf(Diary.MES) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentPage = Diary.values()[page]
        }
    }

    Column(modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = currentPage.ordinal,
            indicator = @Composable { tabPositions ->
                if (currentPage.ordinal < tabPositions.size) {
                    val width by animateDpAsState(
                        targetValue = tabPositions[currentPage.ordinal].contentWidth,
                        label = "tabIndicatorAnimation"
                    )
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentPage.ordinal]),
                        width = width
                    )
                }
            }
        ) {
            Diary.values().forEach {
                Tab(
                    selected = currentPage == it,
                    onClick = {
                        currentPage = it
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage.ordinal)
                        }
                    },
                    text = { Text(stringResource(id = it.title)) },
                    icon = { Icon(it.icon, stringResource(it.title)) }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            Column(
                Modifier.fillMaxSize(),
                Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .width(TextFieldDefaults.MinWidth)
                        .height(TextFieldDefaults.MinHeight)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            Brush.linearGradient(
                                Diary.values()[page].primaryLogGradientColors
                                    .map { colorResource(it) }), MaterialTheme.shapes.medium
                        )
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { Diary.values()[page].primaryLogInFunction(context) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (page == Diary.MES.ordinal) {
                        Image(
                            painterResource(R.drawable.ic_mos_ru),
                            stringResource(id = R.string.log_in),
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .width(16.dp)
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = stringResource(id = R.string.log_in),
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                            tint = Color.White
                        )
                    }
                    Text(
                        stringResource(id = Diary.values()[page].primaryLogInLabel),
                        color = Color.White
                    )
                }

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
                        stringResource(id = R.string.or),
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

                Diary.values()[page].alternativeLogIn(
                    Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
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