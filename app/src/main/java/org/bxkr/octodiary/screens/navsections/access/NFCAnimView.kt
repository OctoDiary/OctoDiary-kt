package org.bxkr.octodiary.screens.navsections.access


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun NFCAnimView(
    isScanning: Boolean,
    hasCard: Boolean,
    isProtected: Boolean
) {
    // Стадии: ожидание -> приближение -> успех/отмена -> возврат
    var stage by remember { mutableStateOf("waiting") } // waiting, approaching, success, error, returning
    val cardX = remember { Animatable(-150f) }
    val cardY = remember { Animatable(-180f) }
    var showCard by remember { mutableStateOf(false) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            showCard = true
            stage = "waiting"
            cardX.snapTo(-150f); cardY.snapTo(-180f)
            delay(400)
            stage = "approaching"
            cardX.animateTo(0f, animationSpec = tween(700))
            cardY.animateTo(0f, animationSpec = tween(700))
            delay(300)
            stage = if (isProtected) "error" else if (hasCard) "success" else "waiting"
            delay(800)
            stage = "returning"
            cardX.animateTo(-150f, animationSpec = tween(500))
            cardY.animateTo(-180f, animationSpec = tween(500))
            delay(400)
            showCard = false
            stage = if (hasCard) "success" else if (isProtected) "error" else "waiting"
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(340.dp), // БОЛЬШЕ
        contentAlignment = Alignment.Center
    ) {
        // Турникет (фон)
        Canvas(Modifier.fillMaxWidth().height(240.dp)) {
            // Столб
            drawRoundRect(
                color = Color(0xFFe0e0e0),
                topLeft = Offset(size.width/2-18, size.height-80),
                size = androidx.compose.ui.geometry.Size(36f, 80f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )
            // Круг NFC
            drawCircle(
                color = when (stage) {
                    "success" -> Color(0xFF22c55e)
                    "error" -> Color(0xFFef4444)
                    else -> Color(0xFFfb923c)
                },
                radius = 38f,
                center = Offset(size.width/2, size.height-80)
            )
        }
        // Карта — только во время сканирования
        if (showCard) {
            Canvas(Modifier.size(260.dp, 160.dp)) {
                translate(cardX.value, cardY.value) {
                    drawRoundRect(
                        color = Color(0xFF374151),
                        topLeft = Offset(size.width/2-90, size.height/2-55),
                        size = androidx.compose.ui.geometry.Size(180f, 110f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                    )
                    // NFC-волна
                    drawArc(
                        color = Color.White,
                        startAngle = 220f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(size.width/2+40, size.height/2-40),
                        size = androidx.compose.ui.geometry.Size(32f, 32f),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }
        // Состояние
        when (stage) {
            "waiting", "approaching", "returning" -> Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(44.dp).rotate(90f), tint = Color(0xFFfb923c))
            "success" -> Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(44.dp), tint = Color(0xFF22c55e))
            "error" -> Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(44.dp), tint = Color(0xFFef4444))
        }
    }
}



