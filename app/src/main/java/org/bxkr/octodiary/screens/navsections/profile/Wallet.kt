package org.bxkr.octodiary.screens.navsections.profile


import androidx.compose.material.icons.Icons
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.formatToLongHumanDateNoTime
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.models.daysbalanceinfo.DaysBalanceInfo
import org.bxkr.octodiary.parseFromDay
import java.util.Calendar
import java.util.Date

private val topUpsLive = MutableLiveData<DaysBalanceInfo?>()
private val isLoadingLive = MutableLiveData(true)


@Composable
fun Wallet() {
    val balance = DataService.mealBalance
    val isLoading by isLoadingLive.observeAsState(true)
    val topUps by topUpsLive.observeAsState()
    val clipboardManager = LocalClipboardManager.current
    val isDemo = LocalContext.current.isDemo

    LaunchedEffect(Unit) {
        if (topUps == null && !isDemo) DataService.getBalanceHistory {
            topUpsLive.value = it
            isLoadingLive.value = false
        } else if (isDemo) isLoadingLive.value = false
    }

    Box(
        Modifier
            .padding(16.dp, 16.dp, 16.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(stringResource(R.string.wallet), style = MaterialTheme.typography.titleMedium)
            NameAndValueText(
                stringResource(R.string.balance),
                stringResource(R.string.balance_t, (balance.balance / 100f).toString())
            )
            NameAndValueText(
                stringResource(R.string.account_number),
                balance.clientId.contractId.toString()
            ) {
                clipboardManager.setText(AnnotatedString(balance.clientId.contractId.toString()))
            }
            Spacer(Modifier.size(16.dp))
            LazyColumn(
                Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    (topUps?.days
                        ?: emptyList()).filter { it.transactions.any { tx -> tx.type == "TOPUP" } },
                    { it.date }) { day ->
                    OutlinedCard(
                        Modifier
                            .fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            NameAndValueText(
                                stringResource(R.string.top_up),
                                day.date.parseFromDay().formatToLongHumanDateNoTime()
                            )
                            day.transactions.forEach { tx ->
                                if (tx.type == "TOPUP") {
                                    Text(
                                        stringResource(
                                            R.string.balance_t,
                                            (tx.sum / 100).toString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                if (topUps?.hasNextPage == true) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton({
                                val lastDay =
                                    (topUps?.days?.last()?.date
                                        ?: Date().formatToDay()).parseFromDay()
                                val dayToRequest = Calendar.getInstance().apply {
                                    time = lastDay
                                    add(Calendar.DAY_OF_YEAR, -1)
                                }.time.formatToDay()
                                isLoadingLive.value = true
                                DataService.getBalanceHistory(dayToRequest) {
                                    isLoadingLive.value = false
                                    topUpsLive.value = topUps?.copy(
                                        days = (topUps?.days ?: emptyList()) + (it.days
                                            ?: emptyList()),
                                        hasNextPage = it.hasNextPage
                                    )
                                }
                            }, enabled = !isLoading) {
                                Text(stringResource(R.string.show_more))
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.size(8.dp))
                }
            }
        }
        AnimatedVisibility(
            isLoading,
            Modifier
                .align(Alignment.TopEnd)
                .size(16.dp)
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }
    }
}


