package org.bxkr.octodiary.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.AuthenticationResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.Gson
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.LocalActivity
import org.bxkr.octodiary.NavSection
import org.bxkr.octodiary.R
import org.bxkr.octodiary.Screen
import org.bxkr.octodiary.UpdateReceiver
import org.bxkr.octodiary.authPrefs
import org.bxkr.octodiary.cachePrefs
import org.bxkr.octodiary.get
import org.bxkr.octodiary.logOut
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.navControllerLive
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.network.interfaces.DSchoolAPI
import org.bxkr.octodiary.network.interfaces.MainSchoolAPI
import org.bxkr.octodiary.network.interfaces.SchoolSessionAPI
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import org.bxkr.octodiary.notificationPrefs
import org.bxkr.octodiary.reloadEverythingLive
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screenLive
import org.bxkr.octodiary.screens.navsections.daybook.customScheduleRefreshListenerLive
import org.bxkr.octodiary.screens.navsections.daybook.updatedScheduleLive
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme
import java.util.Calendar
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavScreen(modifier: Modifier, pinFinished: MutableState<Boolean>) {
    with(LocalContext.current) {
        val initialPin = remember { mutableStateOf(emptyList<Int>()) }
        val secondPin = remember { mutableStateOf(emptyList<Int>()) }
        val navController = navControllerLive.observeAsState()

        if (initialPin.value.size == 4 && secondPin.value.size == 4) {
            if (initialPin.value == secondPin.value) {
                pinFinished.value = true
                mainPrefs.save(
                    "has_pin" to true,
                    "pin" to secondPin.value.joinToString(""),
                    "first_launch" to false
                )
            } else {
                initialPin.value = emptyList()
                secondPin.value = emptyList()
                pinFinished.value = false

            }
        }

        Surface(modifier.fillMaxSize()) {
            if (mainPrefs.get<Boolean>("has_pin") != true || pinFinished.value) {
                DataService.token = authPrefs.get<String>("access_token")!!

                DataService.subsystem = Diary.values()[authPrefs.get<Int>("subsystem")!!]

                val diary = DataService.subsystem
                DataService.mainSchoolApi =
                    NetworkService.mainSchoolApi(MainSchoolAPI.getBaseUrl(diary))
                DataService.dSchoolApi = NetworkService.dSchoolApi(DSchoolAPI.getBaseUrl(diary))
                DataService.schoolSessionApi =
                    NetworkService.schoolSessionApi(SchoolSessionAPI.getBaseUrl(diary))
                DataService.secondaryApi =
                    NetworkService.secondaryApi(SecondaryAPI.getBaseUrl(diary))

                var localLoadedState by remember { mutableStateOf(false) }
                val context = LocalContext.current
                if (localLoadedState) {
                    LaunchedEffect(Unit) {
                        if (notificationPrefs.get<Long>("student_id") == null) {
                            notificationPrefs.save(
                                "student_id" to DataService.profile.children[DataService.currentProfile].studentId,
                                "mark_ids" to Gson().toJson(DataService.marksDate.payload.map { it.id }),
                                "total_count" to 0
                            )
                        }
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, UpdateReceiver::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            Calendar.getInstance().timeInMillis,
                            60 * 1000,
                            pendingIntent
                        )
                    }
                }
                LaunchedEffect(rememberCoroutineScope()) {
                    snapshotFlow { DataService.loadedEverything.value }.onEach {
                        localLoadedState = it
                    }.launchIn(this)
                }
                LaunchedEffect(Unit) {
                    reloadEverythingLive.postValue {
                        DataService.run {
                            loadingStarted = false
                            loadedEverything.value = false

                        }
                    }
                }
                AnimatedVisibility(localLoadedState) {
                    val refreshState = rememberPullToRefreshState()
                    var defaultRefresh by remember { mutableStateOf(true) }
                    if (refreshState.isRefreshing) {
                        when (navController.value?.currentDestination?.route) {
                            NavSection.Daybook.route -> {
                                val customScheduleRefreshListener =
                                    customScheduleRefreshListenerLive.value
                                if (customScheduleRefreshListener != null) {
                                    customScheduleRefreshListener()
                                } else {
                                    defaultRefresh = false
                                    DataService.updateEventCalendar {
                                        updatedScheduleLive.postValue(
                                            updatedScheduleLive.value?.not() ?: false
                                        )
                                        refreshState.endRefresh()
                                    }
                                }
                            }

                            else -> {
                                defaultRefresh = true
                                DataService.loadedEverything.value = false
                                DataService.loadingStarted = false
                                DataService.updateAll(context)
                            }
                        }
                    }
                    if (DataService.loadedEverything.value && defaultRefresh) {
                        refreshState.endRefresh()
                    }
                    LaunchedEffect(Unit) {
                        DataService.sendStatistic(context.mainPrefs.get<String>("deviceId")!!) {}
                    }
                    NavHost(
                        navController = navController.value!!,
                        startDestination = NavSection.Dashboard.route
                    ) {
                        NavSection.values().forEach {
                            composable(it.route) { _ ->
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .nestedScroll(refreshState.nestedScrollConnection)
                                ) {
                                    it.composable()
                                    PullToRefreshContainer(
                                        refreshState, Modifier.align(Alignment.TopCenter)
                                    )
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(!localLoadedState) {
                    var progress by remember { mutableFloatStateOf(0f) }
                    val progressAnimated by animateFloatAsState(
                        progress, tween(200), label = "progress_anim"
                    )
                    val coroutineScope = rememberCoroutineScope()
                    DataService.onSingleItemInUpdateAllLoadedHandler = { name, progressParam ->
                        coroutineScope.launch {
                            progress = progressParam
                        }
                        cachePrefs.save(
                            name to Gson().toJson(
                                DataService::class.java.getDeclaredField(name).get(DataService)
                            ), "age" to System.currentTimeMillis()
                        )
                    }

                    val activity = LocalActivity.current
                    DataService.tokenExpirationHandler = {
                        activity.logOut("Performed from token expiration handler")
                    }

                    if (!DataService.loadingStarted) {
                        if (cachePrefs.get<Long>("age")
                                ?.let { (System.currentTimeMillis() - it) < 86400000 } == true
                        ) {
                            DataService.loadingStarted = true
                            DataService.subsystem =
                                Diary.values()[authPrefs.get<Int>("subsystem") ?: 0]
                            DataService.loadFromCache { cachePrefs.get<String>(it) ?: "" }
                            DataService.loadedEverything.value = true
                        } else {
                            DataService.updateAll(context)
                        }
                    }
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LinearProgressIndicator(
                            progress = { progressAnimated },
                        )
                    }
                }
                screenLive.value = Screen.MainNav
            } else if (mainPrefs.get<Boolean>("has_pin") == true && !pinFinished.value) {
                EnterPinDialog(pinFinished = pinFinished)
            }
            if ((mainPrefs.get<Boolean>("first_launch") == true) && !pinFinished.value) {
                SetPinDialog(
                    pinFinished = pinFinished, initialPin = initialPin, secondPin = secondPin
                )
            }
        }
    }
}

@Composable
fun EnterPinDialog(
    pinFinished: MutableState<Boolean>
) {
    val currentPin = remember { mutableStateOf(emptyList<Int>()) }
    var wrongPin by remember { mutableStateOf(false) }
    val fpShown = remember { mutableStateOf(false) }
    if (currentPin.value.size == 4) {
        if (currentPin.value.joinToString("") == LocalContext.current.mainPrefs.get<String>("pin")) {
            pinFinished.value = true
        } else {
            currentPin.value = emptyList()
            wrongPin = !wrongPin
        }
    }

    Dialog(properties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    ), onDismissRequest = { pinFinished.value = true }) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                val activity = LocalActivity.current
                FilledTonalButton(
                    modifier = Modifier.padding(16.dp), onClick = {
                        activity.logOut("Passcode is forgotten")
                    }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Logout,
                        stringResource(id = R.string.log_out),
                        Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(id = R.string.log_out),
                    )
                }

                AnimatedVisibility(
                    visible = !wrongPin,
                    label = "pin_screen_animation",
                    exit = fadeOut(tween(300)),
                    enter = fadeIn(tween(300))
                ) {
                    PinScreen(Modifier.fillMaxHeight(), into = currentPin, fp = true, fpShown)
                }
                AnimatedVisibility(
                    visible = wrongPin,
                    label = "pin_screen_animation",
                    exit = fadeOut(tween(300)),
                    enter = fadeIn(tween(300))
                ) {
                    PinScreen(Modifier.fillMaxHeight(), into = currentPin, fp = true, fpShown)
                }
            }
        }
    }
}

@Composable
fun SetPinDialog(
    pinFinished: MutableState<Boolean>,
    initialPin: MutableState<List<Int>>,
    secondPin: MutableState<List<Int>>,
    closeButtonTitle: String = stringResource(id = R.string.skip)
) {
    val context = LocalContext.current
    Dialog(properties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false
    ), onDismissRequest = { pinFinished.value = true }) {
        Surface {
            Column(
                Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp), horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = {
                            pinFinished.value = true
                            context.mainPrefs.save(
                                "has_pin" to false, "first_launch" to false
                            )
                        }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            closeButtonTitle,
                            Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(closeButtonTitle)
                    }
                }
                AnimatedContent(
                    targetState = initialPin, label = "title_animation"
                ) { targetState ->
                    Text(
                        stringResource(
                            if (targetState.value.size == 4) {
                                R.string.repeat_pin
                            } else R.string.enter_new_pin
                        ),
                        Modifier.padding(vertical = 64.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                AnimatedContent(
                    targetState = initialPin, label = "pin_screen_animation"
                ) { targetState ->
                    if (targetState.value.size >= 4) {
                        PinScreen(
                            Modifier.fillMaxSize(), secondPin
                        )
                    } else PinScreen(
                        Modifier.fillMaxSize(), targetState
                    )
                }
            }
        }
    }
}

@Composable
fun PinScreen(
    modifier: Modifier = Modifier,
    into: MutableState<List<Int>>,
    fp: Boolean = false,
    fpShown: MutableState<Boolean> = mutableStateOf(false)
) {
    val enteredNumbers = remember { mutableStateOf(emptyList<Int>()) }
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalActivity.current
    val biometricManager = BiometricManager.from(context)
    val isBiometricAvailable = biometricManager.canAuthenticate(
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK
    ) == BiometricManager.BIOMETRIC_SUCCESS

    if (enteredNumbers.value.size >= 4) {
        into.value = enteredNumbers.value.take(4)
    }

    if (!fpShown.value && context.mainPrefs.get<String>("pin") != null) {
        fpShown.value = true
        if (isBiometricAvailable) {
            biometricAuth(context, enteredNumbers)
        }
    }

    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .padding(32.dp)
                .zIndex(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            (0..3).forEach {
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.run {
                                if (enteredNumbers.value.size > it) {
                                    secondary
                                } else secondaryContainer
                            }, MaterialTheme.shapes.medium
                        )
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            (0..9).toList().also { Collections.rotate(it, -1) }.chunked(3).forEach { triplet ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (triplet[0] == 0 && isBiometricAvailable && fp) {
                        Icon(Icons.Rounded.Fingerprint,
                            stringResource(R.string.fingerprint),
                            Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable {
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                    biometricAuth(context, enteredNumbers)
                                }
                                .padding(16.dp),
                            MaterialTheme.colorScheme.secondary)
                    } else if (triplet[0] == 0) {
                        Box(Modifier.size(72.dp))
                    }
                    triplet.forEach {
                        PinKeyboardButton(it, enteredNumbers)
                    }
                    if (triplet[0] == 0) {
                        Icon(Icons.AutoMirrored.Rounded.Backspace,
                            stringResource(R.string.cancel),
                            Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable {
                                    removeNumber(enteredNumbers)
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                }
                                .padding(24.dp),
                            MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun PinKeyboardButton(number: Int, enteredNumbers: MutableState<List<Int>>) {
    val hapticFeedback = LocalHapticFeedback.current
    Row(
        Modifier
            .padding(8.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(
                remember { MutableInteractionSource() },
                enabled = enteredNumbers.value.size < 4,
                indication = rememberRipple(
                    true, 64.dp, MaterialTheme.colorScheme.surfaceContainer
                ),
            ) {
                clickNumber(number, enteredNumbers)
                hapticFeedback.performHapticFeedback(
                    HapticFeedbackType.LongPress
                )
            }
            .background(
                MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraLarge
            )
            .alpha(
                if (enteredNumbers.value.size >= 4) .3f
                else 1f
            )
            .padding(
                horizontal = 24.dp, vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun clickNumber(number: Int, enteredNumbers: MutableState<List<Int>>) {
    enteredNumbers.value += listOf(number)
}

fun removeNumber(enteredNumbers: MutableState<List<Int>>) {
    if (enteredNumbers.value.isNotEmpty()) {
        enteredNumbers.value = enteredNumbers.value.subList(0, enteredNumbers.value.lastIndex)
    }
}

fun biometricAuth(context: FragmentActivity, enteredNumbers: MutableState<List<Int>>) {
    with(context) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(this, executor, object : AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val pin = mainPrefs.get<String>("pin")
                    if (pin != null) {
                        enteredNumbers.value = pin.map { it.digitToInt() }
                    }
                }
            })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.fingerprint))
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL or BIOMETRIC_WEAK)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO, locale = "ru"
)
@Composable
fun PinDialogPreview() {
    OctoDiaryTheme {
        Surface(Modifier.fillMaxSize()) {
            SetPinDialog(remember { mutableStateOf(false) },
                remember { mutableStateOf(emptyList()) },
                remember { mutableStateOf(emptyList()) })
        }
    }
}