package org.bxkr.octodiary.screens.navsections.profile


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Wallet
import android.annotation.SuppressLint
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.R
import org.bxkr.octodiary.baseEnqueueOrNull
import org.bxkr.octodiary.launchPickerLive
import org.bxkr.octodiary.launchUrlLive
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.modalDialogContentLive
import org.bxkr.octodiary.modalDialogStateLive
import org.bxkr.octodiary.models.profile.Children
import org.bxkr.octodiary.network.NetworkService
import org.bxkr.octodiary.screens.navsections.profile.meal.MealDialog

val avatarTriggerLive = MutableLiveData(false)

@Composable
fun ProfileScreen2() {
    val child = DataService.profile.children[DataService.currentProfile]
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ShortProfileInfo(child)
        Cards()
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ShortProfileInfo(child: Children) {
    val trigger = avatarTriggerLive.observeAsState()
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.padding(end = 16.dp)) {
            val onAvatarClick = {
                modalDialogContentLive.value = {
                    var loading by remember { mutableStateOf(false) }
                    AlertDialog(
                        {
                            modalDialogStateLive.postValue(false)
                        },
                        confirmButton = {
                            TextButton(
                                { modalDialogStateLive.postValue(false) },
                                enabled = !loading
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                        title = { Text(stringResource(R.string.avatar)) },
                        text = {
                            Column {
                                AnimatedVisibility(loading) {
                                    Column(
                                        Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                AnimatedVisibility(!loading) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val actionCard =
                                            @Composable { onClick: () -> Unit, text: Int ->
                                                OutlinedCard(
                                                    onClick,
                                                    Modifier
                                                        .fillMaxWidth()
                                                ) {
                                                    Row(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(stringResource(text))
                                                    }
                                                }
                                            }
                                        actionCard(
                                            {
                                                loading = true
                                                launchPickerLive.value?.invoke()
                                            },
                                            R.string.choose_from_gallery
                                        )
                                        if (DataService.avatars.isNotEmpty()) {
                                            actionCard({
                                                loading = true
                                                DataService.run {
                                                    secondaryApi.deleteAvatar(
                                                        "Bearer $token",
                                                        profile.children[currentProfile].contingentGuid,
                                                        avatars.first().id.toString()
                                                    ).baseEnqueueOrNull {
                                                        updateAvatars {
                                                            modalDialogStateLive.postValue(false)
                                                            avatarTriggerLive.value =
                                                                avatarTriggerLive.value?.not()
                                                                    ?: true
                                                        }
                                                    }
                                                }
                                            }, R.string.delete)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                modalDialogStateLive.postValue(true)
            }
            AnimatedContent(trigger.value, label = "avatar_anim") {
                DataService.avatars.let {
                    if (it.isNotEmpty()) {
                        GlideImage(
                            it.first().url.let { string -> Uri.parse(string) },
                            stringResource(R.string.avatar),
                            Modifier
                                .clip(CircleShape)
                                .clickable { onAvatarClick() }
                                .size(64.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable { onAvatarClick() }
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_launcher_foreground),
                                stringResource(R.string.app_name),
                                Modifier.scale(1.4f),
                                MaterialTheme.colorScheme.run { secondary }
                            )
                        }
                    }
                }
            }
        }
        Column {
            Text(
                child.run { "$lastName $firstName $middleName" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(child.school.shortName)
            Text(stringResource(R.string.class_t, child.className))
        }
    }
}

@Composable
private fun Cards() {
    val is77 = remember { DataService.subsystem == Diary.MES }
    Column(
        Modifier
            .padding(16.dp)
            .clip(MaterialTheme.shapes.large),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ProfileCard(R.string.personal_data, Icons.Default.Person) { PersonalData() }
        ProfileCard(R.string.class_label, Icons.Default.Group) { ClassInfo() }
        ProfileCard(R.string.school_and_teachers, Icons.Default.School) { School() }
        if (isExamsNotEmpty()) ProfileCard(
            R.string.exam_results,
            Icons.Default.Grade
        ) { ExamResults() }
        if (is77) ProfileCard(R.string.wallet, Icons.Default.Wallet) { Wallet() }
        ProfileCard(R.string.meal, Icons.Default.Restaurant, onClick = ::mealOnClick)
        ProfileCard(R.string.documents, Icons.Default.Description) { Documents() }
    }
}

private fun mealOnClick() {
    if (DataService.subsystem == Diary.MES) {
        modalDialogContentLive.value = { MealDialog() }
        modalDialogStateLive.postValue(true)
    } else {
        launchUrlLive.postValue(Uri.parse(NetworkService.MySchoolAPIConfig.FOOD_URI))
    }
}

@Composable
private fun ProfileCard(
    @StringRes textRes: Int,
    icon: ImageVector,
    bottomSheetContent: @Composable () -> Unit,
) {
    ProfileCard(stringResource(textRes), icon, bottomSheetContent)
}

@Composable
private fun ProfileCard(
    @StringRes textRes: Int,
    icon: ImageVector,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    ProfileCard(stringResource(textRes), icon, onLongClick, onClick)
}

@Composable
private fun ProfileCard(
    text: String,
    icon: ImageVector,
    bottomSheetContent: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.extraSmall
            )
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable { openBottomSheet { bottomSheetContent() } }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.padding(end = 8.dp), MaterialTheme.colorScheme.onSurface)
        Text(text)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileCard(
    text: String,
    icon: ImageVector,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.extraSmall
            )
            .clip(MaterialTheme.shapes.extraSmall)
            .combinedClickable(
                onLongClick = {
                    android.util.Log.d("ProfileCard", "Long click on card: $text")
                    onLongClick()
                },
                onClick = {
                    android.util.Log.d("ProfileCard", "Click on card: $text")
                    onClick()
                }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.padding(end = 8.dp), MaterialTheme.colorScheme.onSurface)
        Text(text)
    }
}

private fun isExamsNotEmpty(): Boolean = DataService.govExams.data.isNotEmpty()

private fun openBottomSheet(content: @Composable () -> Unit) {
    modalBottomSheetStateLive.postValue(true)
    modalBottomSheetContentLive.postValue(content)
}


