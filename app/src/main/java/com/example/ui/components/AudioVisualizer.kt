package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaViolet
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    isActive: Boolean,
    isSpeaking: Boolean = false,
    audioRms: Float = 0f,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    barCount: Int = 24
) {
    val barAnimatables = remember {
        List(barCount) { Animatable(0.2f) }
    }

    LaunchedEffect(isActive, isSpeaking, audioRms) {
        if (isActive) {
            barAnimatables.forEachIndexed { index, animatable ->
                val base = if (isSpeaking) {
                    // Simulated vocal waveform pattern
                    0.25f + 0.65f * kotlin.math.abs(kotlin.math.sin((index * 0.45f) + (System.currentTimeMillis() % 1000) / 200f))
                } else {
                    // Microphone RMS driven with jitter
                    (audioRms * 0.8f) + Random.nextFloat() * 0.25f
                }.coerceIn(0.15f, 1.0f)

                animatable.animateTo(
                    targetValue = base,
                    animationSpec = tween(
                        durationMillis = 120 + (index % 4) * 20,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        } else {
            barAnimatables.forEach { it.animateTo(0.15f, tween(150)) }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.8f)).coerceIn(3.dp.toPx(), 8.dp.toPx())
        val spacing = (totalWidth - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)

        val brush = Brush.verticalGradient(
            colors = if (isSpeaking) {
                listOf(NovaMagenta, NovaViolet, NovaCyan)
            } else {
                listOf(NovaCyan, NovaViolet)
            }
        )

        for (i in 0 until barCount) {
            val fraction = barAnimatables[i].value
            val currentBarHeight = (canvasHeight * fraction).coerceAtLeast(4.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = (canvasHeight - currentBarHeight) / 2f

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
