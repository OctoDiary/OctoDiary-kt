package org.bxkr.octodiary.screens.navsections.daybook


import androidx.compose.material.icons.Icons
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun CalendarHandle(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary
    Box(modifier.padding(bottom = 16.dp)) {
        val anim = remember { Animatable(20f) }
        Surface(
            Modifier
                .size(24.dp, 4.dp)
                .offset(-10.dp)
                .rotate(anim.value), CircleShape, color
        ) {}
        Surface(
            Modifier
                .size(24.dp, 4.dp)
                .offset(10.dp)
                .rotate(-anim.value), CircleShape, color
        ) {}
    }
}


