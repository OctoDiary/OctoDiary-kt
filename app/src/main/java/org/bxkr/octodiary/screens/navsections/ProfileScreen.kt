package org.bxkr.octodiary.screens.navsections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService

@Composable
fun ProfileScreen() {
    Column(
        Modifier
            .fillMaxSize()
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
                    "Привет, ${profile.children[0].firstName}!",
                    style = MaterialTheme.typography.titleLarge
                ) // FUTURE: USES_FIRST_CHILD UNTRANSLATED
                Text(
                    "${profile.children[0].className} класс",
                    style = MaterialTheme.typography.titleMedium
                ) // FUTURE: USES_FIRST_CHILD UNTRANSLATED
                Text("${mealBalance.balance / 100} ₽", style = MaterialTheme.typography.titleSmall)
            }
        }
        Column(
            Modifier.clip(MaterialTheme.shapes.extraLarge),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                SectionGridItem("Личные данные", Icons.Rounded.Person) {

                } // FUTURE: UNTRANSLATED
                SectionGridItem("Класс", Icons.Rounded.Group) {

                } // FUTURE: UNTRANSLATED
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                SectionGridItem("Еда", Icons.Rounded.Restaurant) {

                } // FUTURE: UNTRANSLATED
                SectionGridItem("Школа", Icons.Rounded.Apartment) {

                } // FUTURE: UNTRANSLATED
                SectionGridItem("Документы", Icons.Rounded.Description) {

                } // FUTURE: UNTRANSLATED
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