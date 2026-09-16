package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaViolet
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun GlowingOrbView(
    state: OrbState,
    audioRms: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")

    // Continuous rotation for thinking or idle
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == OrbState.THINKING) 3000 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Breathing pulse
    val breathePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbBreathe"
    )

    // Audio reactive expansion for listening / speaking
    val audioExpansion = remember { Animatable(0f) }
    LaunchedEffect(audioRms) {
        audioExpansion.animateTo(
            targetValue = audioRms * 0.4f,
            animationSpec = tween(durationMillis = 80)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("glowing_orb")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 3.4f
            val dynamicScale = when (state) {
                OrbState.IDLE -> breathePulse
                OrbState.LISTENING -> 1.0f + audioExpansion.value + 0.05f
                OrbState.THINKING -> breathePulse * 1.02f
                OrbState.SPEAKING -> 1.05f + (sin(Math.toRadians(rotation.toDouble() * 3)).toFloat() * 0.08f)
            }
            val radius = baseRadius * dynamicScale

            // Colors based on state
            val (glowStart, glowMid, glowEnd) = when (state) {
                OrbState.IDLE -> Triple(NovaCyan, NovaViolet, Color(0xFF131131))
                OrbState.LISTENING -> Triple(NovaCyan, NovaEmerald, Color(0xFF0D2538))
                OrbState.THINKING -> Triple(NovaViolet, NovaMagenta, Color(0xFF26123D))
                OrbState.SPEAKING -> Triple(NovaMagenta, NovaCyan, Color(0xFF330C28))
            }

            // 1. Outer ambient diffuse halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowStart.copy(alpha = if (state == OrbState.IDLE) 0.35f else 0.55f),
                        glowMid.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 2.1f
                ),
                radius = radius * 2.1f,
                center = center
            )

            // 2. Outer Orbital Ring 1
            drawCircle(
                color = glowStart.copy(alpha = 0.4f),
                radius = radius * 1.5f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Gyroscopic Ring with rotating accent dots
            val rad1 = Math.toRadians(rotation.toDouble())
            val dotX = center.x + (radius * 1.5f * cos(rad1)).toFloat()
            val dotY = center.y + (radius * 1.5f * sin(rad1)).toFloat()
            drawCircle(
                color = glowStart,
                radius = 3.5.dp.toPx(),
                center = Offset(dotX, dotY)
            )

            val rad2 = Math.toRadians((rotation + 180).toDouble())
            val dotX2 = center.x + (radius * 1.5f * cos(rad2)).toFloat()
            val dotY2 = center.y + (radius * 1.5f * sin(rad2)).toFloat()
            drawCircle(
                color = glowMid,
                radius = 2.5.dp.toPx(),
                center = Offset(dotX2, dotY2)
            )

            // 4. Secondary Tilted Orbital Ring
            drawOval(
                color = glowMid.copy(alpha = 0.35f),
                topLeft = Offset(center.x - radius * 1.4f, center.y - radius * 0.75f),
                size = androidx.compose.ui.geometry.Size(radius * 2.8f, radius * 1.5f),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // 5. Core AI Orb with vibrant linear gradient
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(glowStart, glowMid, glowEnd),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius)
                ),
                radius = radius,
                center = center
            )

            // 6. Glowing Inner Nucleus / Specular Reflection
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        glowStart.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                    radius = radius * 0.75f
                ),
                radius = radius * 0.75f,
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
            )

            // 7. Center Energy Crosshair for NOVA-X
            val crossSize = radius * 0.35f
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(center.x - crossSize, center.y),
                end = Offset(center.x + crossSize, center.y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(center.x, center.y - crossSize),
                end = Offset(center.x, center.y + crossSize),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}
