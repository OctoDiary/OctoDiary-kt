package org.bxkr.octodiary.screens.navsections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.profile.ClassInfo
import org.bxkr.octodiary.components.profile.PersonalData
import org.bxkr.octodiary.components.profile.School
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.snackbarHostStateLive

@Composable
fun ProfileScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp), verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            with(DataService) {
                Text(
                    stringResource(R.string.hello_t, profile.children[currentProfile].firstName),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    stringResource(R.string.class_t, profile.children[currentProfile].className),
                    style = MaterialTheme.typography.titleMedium
                )
                if (subsystem == Diary.MES) {
                    Text(
                        stringResource(R.string.balance_t, mealBalance.balance / 100),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
        Column(
            Modifier.clip(MaterialTheme.shapes.extraLarge),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                SectionGridItem(stringResource(R.string.personal_data), Icons.Rounded.Person) {
                    openBottomSheet { PersonalData() }
                }
                SectionGridItem(stringResource(R.string.class_label), Icons.Rounded.Group) {
                    openBottomSheet { ClassInfo() }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (DataService.subsystem == Diary.MES) {
                    SectionGridItem(stringResource(R.string.meal), Icons.Rounded.Restaurant) {
                        coroutineScope.launch {
                            snackbarHostStateLive.value?.showSnackbar(context.getString(R.string.soon))
                        }
                    }
                }
                SectionGridItem(stringResource(R.string.school), Icons.Rounded.Apartment) {
                    openBottomSheet { School() }
                }
                SectionGridItem(stringResource(R.string.documents), Icons.Rounded.Description) {
                    openBottomSheet {
                        coroutineScope.launch {
                            snackbarHostStateLive.value?.showSnackbar(context.getString(R.string.soon))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SectionGridItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        Modifier
            .clickable { onClick() }
            .weight(1f),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults
            .cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                icon,
                title,
                modifier = Modifier.padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(title, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

fun openBottomSheet(content: @Composable () -> Unit) {
    modalBottomSheetStateLive.postValue(true)
    modalBottomSheetContentLive.postValue(content)
}