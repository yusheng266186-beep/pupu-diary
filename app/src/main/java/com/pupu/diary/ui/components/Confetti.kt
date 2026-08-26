package com.pupu.diary.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.pupu.diary.ui.theme.LocalAppPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val spin: Float,
    val color: Color,
    val circle: Boolean
)

/**
 * 全屏粒子迸发：trigger > 0 时触发一次。
 * heart = true 时迸出爱心（角色互动用），否则彩色纸屑（保存成功用）。
 */
@Composable
fun ConfettiBurst(
    trigger: Int,
    modifier: Modifier = Modifier,
    originFrac: Offset = Offset(0.5f, 0.55f),
    heart: Boolean = false,
    count: Int = 44,
    onFinished: () -> Unit = {}
) {
    val palette = LocalAppPalette.current
    val progress = remember { Animatable(1f) }
    var active by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            active = true
            progress.animateTo(1f, tween(1400, easing = LinearEasing))
            active = false
            onFinished()
        }
    }

    if (active) {
        val colors = listOf(
            palette.primary, palette.accent, palette.poop,
            Color(0xFFF2A69B), Color(0xFF8FB6D9), palette.primaryDeep
        )
        val params = remember(trigger) {
            val rnd = Random(trigger)
            List(count) {
                Particle(
                    angle = -PI.toFloat() + rnd.nextFloat() * 2f * PI.toFloat(),
                    speed = 0.22f + rnd.nextFloat() * 0.4f,
                    size = 6f + rnd.nextFloat() * 8f,
                    spin = (rnd.nextFloat() - 0.5f) * 3f,
                    color = colors[rnd.nextInt(colors.size)],
                    circle = rnd.nextBoolean()
                )
            }
        }
        Canvas(modifier.fillMaxSize()) {
            val t = progress.value
            val origin = Offset(size.width * originFrac.x, size.height * originFrac.y)
            val gravity = size.height * 1.5f
            params.forEach { p ->
                val dist = p.speed * size.width
                val px = origin.x + cos(p.angle) * dist * t
                val py = origin.y + sin(p.angle) * dist * t * 0.7f + gravity * t * t * 0.5f
                val fade = (1f - ((t - 0.6f) / 0.4f).coerceIn(0f, 1f))
                val alpha = fade.coerceIn(0f, 1f)
                val sz = p.size.dp.toPx() * (1f - t * 0.25f)
                rotate(degrees = p.spin * 360f * t, pivot = Offset(px, py)) {
                    if (heart) {
                        drawHeart(px, py, sz, p.color, alpha)
                    } else if (p.circle) {
                        drawCircle(p.color.copy(alpha = alpha), sz / 2f, Offset(px, py))
                    } else {
                        drawRoundRect(
                            p.color.copy(alpha = alpha),
                            topLeft = Offset(px - sz / 2f, py - sz / 4f),
                            size = Size(sz, sz / 2f),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, s: Float, color: Color, alpha: Float) {
    val w = s
    val h = s
    val p = Path().apply {
        moveTo(cx, cy + h * 0.35f)
        cubicTo(
            cx - w * 0.55f, cy - h * 0.05f,
            cx - w * 0.35f, cy - h * 0.45f,
            cx, cy - h * 0.15f
        )
        cubicTo(
            cx + w * 0.35f, cy - h * 0.45f,
            cx + w * 0.55f, cy - h * 0.05f,
            cx, cy + h * 0.35f
        )
        close()
    }
    drawPath(p, color.copy(alpha = alpha))
}
