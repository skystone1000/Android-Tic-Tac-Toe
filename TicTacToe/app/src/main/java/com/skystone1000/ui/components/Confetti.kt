package com.skystone1000.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Particle(
    val xFraction: Float,
    val color: Color,
    val size: Float,
    val durationMillis: Int,
    val delayFraction: Float,
)

private val confettiColors = listOf(
    Color(0xFF2DD4BF), Color(0xFFFB923C), Color(0xFFFBBF24),
    Color(0xFFA5B4FC), Color(0xFF14B8A6), Color(0xFFF97316),
)

/** Lightweight falling-confetti overlay. Fills its parent; draw it behind dialog content. */
@Composable
fun Confetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 40,
) {
    val particles = remember {
        val rnd = Random(42)
        List(particleCount) {
            Particle(
                xFraction = rnd.nextFloat(),
                color = confettiColors[rnd.nextInt(confettiColors.size)],
                size = 6f + rnd.nextFloat() * 8f,
                durationMillis = 2200 + rnd.nextInt(1600),
                delayFraction = rnd.nextFloat(),
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "fall",
    )

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val t = ((progress + p.delayFraction) % 1f)
            val y = t * (size.height + 40f) - 20f
            val x = p.xFraction * size.width
            drawRect(
                color = p.color,
                topLeft = Offset(x, y),
                size = Size(p.size, p.size * 1.6f),
            )
        }
    }
}
