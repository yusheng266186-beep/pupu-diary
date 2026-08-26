package com.pupu.diary.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pupu.diary.ui.theme.LocalAppPalette

private val MoodBg = listOf(
    Color(0xFFF6C453), Color(0xFFA8C98F), Color(0xFF8FB6D9),
    Color(0xFFE8A87C), Color(0xFFE88B8B)
)
private val FaceInk = Color(0xFF5C4632)

/** 心情脸（0 开心 1 平静 2 一般 3 难受 4 痛苦），选中弹跳 + 圆环 */
@Composable
fun MoodFace(
    mood: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    val haptic = LocalHapticFeedback.current
    val palette = LocalAppPalette.current
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 480f),
        label = "moodScale"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .size(size)
                .semantics {
                    contentDescription = "心情：${
                        com.pupu.diary.data.Moods.labels[mood.coerceIn(0, 4)]
                    }"
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
        ) {
            val s = this.size.minDimension / 100f
            // 白色描边让脸从卡片底色里浮出来
            drawCircle(Color.White, radius = 54f * s)
            if (selected) {
                drawCircle(palette.primary.copy(alpha = 0.35f), radius = 58f * s)
            }
            drawCircle(MoodBg[mood.coerceIn(0, 4)], radius = 48f * s)
            val ink = FaceInk
            when (mood) {
                0 -> {
                    arcEye(this, 36f, 44f, s, ink)
                    arcEye(this, 64f, 44f, s, ink)
                    drawArc(
                        ink, 0f, 180f, true,
                        topLeft = Offset(34f * s, 52f * s), size = Size(32f * s, 26f * s)
                    )
                }
                1 -> {
                    dotEye(this, 37f, 44f, s, ink); dotEye(this, 63f, 44f, s, ink)
                    drawArc(
                        ink, 40f, 100f, false,
                        topLeft = Offset(36f * s, 52f * s), size = Size(28f * s, 24f * s),
                        style = Stroke(4.5f * s, cap = StrokeCap.Round)
                    )
                }
                2 -> {
                    dotEye(this, 37f, 44f, s, ink); dotEye(this, 63f, 44f, s, ink)
                    drawLine(
                        ink,
                        Offset(38f * s, 64f * s), Offset(62f * s, 64f * s),
                        strokeWidth = 4.5f * s, cap = StrokeCap.Round
                    )
                }
                3 -> {
                    line(this, 31f, 33f, 41f, 38f, s, ink)
                    line(this, 69f, 33f, 59f, 38f, s, ink)
                    dotEye(this, 37f, 48f, s, ink); dotEye(this, 63f, 48f, s, ink)
                    drawArc(
                        ink, 210f, 120f, false,
                        topLeft = Offset(35f * s, 56f * s), size = Size(30f * s, 26f * s),
                        style = Stroke(4.5f * s, cap = StrokeCap.Round)
                    )
                }
                else -> {
                    xEye(this, 36f, 44f, s, ink); xEye(this, 64f, 44f, s, ink)
                    drawArc(
                        ink, 210f, 120f, false,
                        topLeft = Offset(35f * s, 58f * s), size = Size(30f * s, 26f * s),
                        style = Stroke(4.5f * s, cap = StrokeCap.Round)
                    )
                    drawCircle(Color(0xFF8FB6D9), radius = 5f * s, center = Offset(68f * s, 56f * s))
                }
            }
        }
    }
}

private fun dotEye(d: DrawScope, x: Float, y: Float, s: Float, ink: Color) {
    d.drawCircle(ink, radius = 4.6f * s, center = Offset(x * s, y * s))
}

private fun arcEye(d: DrawScope, x: Float, y: Float, s: Float, ink: Color) {
    d.drawArc(
        ink, 200f, 140f, false,
        topLeft = Offset((x - 6f) * s, (y - 5f) * s), size = Size(12f * s, 10f * s),
        style = Stroke(4.2f * s, cap = StrokeCap.Round)
    )
}

private fun xEye(d: DrawScope, x: Float, y: Float, s: Float, ink: Color) {
    d.drawLine(ink, Offset((x - 5f) * s, (y - 5f) * s), Offset((x + 5f) * s, (y + 5f) * s), 4.2f * s, StrokeCap.Round)
    d.drawLine(ink, Offset((x + 5f) * s, (y - 5f) * s), Offset((x - 5f) * s, (y + 5f) * s), 4.2f * s, StrokeCap.Round)
}

private fun line(d: DrawScope, x1: Float, y1: Float, x2: Float, y2: Float, s: Float, ink: Color) {
    d.drawLine(ink, Offset(x1 * s, y1 * s), Offset(x2 * s, y2 * s), 4f * s, StrokeCap.Round)
}
