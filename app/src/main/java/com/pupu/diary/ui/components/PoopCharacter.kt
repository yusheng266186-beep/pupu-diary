package com.pupu.diary.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pupu.diary.ui.theme.AppPalette
import com.pupu.diary.ui.theme.LocalAppPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val FaceDark = Color(0xFF4A3728)
private val Blush = Color(0xFFF2A69B)

/**
 * 便便主角：三层软冰淇淋漩涡 + 卷曲尖 + 表情（对齐参考稿造型）。
 * 自带：呼吸（缩放）、随机眨眼、点击跳跃 + 落地挤压、庆祝摇摆（张嘴）。
 */
@Composable
fun PoopCharacter(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    mood: Int = 0,
    celebrate: Boolean = false,
    breathing: Boolean = true,
    idleHop: Boolean = false,
    blinking: Boolean = true,
    onTap: (() -> Unit)? = null,
    palette: AppPalette = LocalAppPalette.current
) {
    val breathT = rememberInfiniteTransition(label = "breath")
    val breath = breathT.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathV"
    )
    val wiggleT = rememberInfiniteTransition(label = "wiggle")
    val wiggle = wiggleT.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(240, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wiggleV"
    )
    val eyeScale = remember { Animatable(1f) }
    // v4.0.0：眨眼循环可关——滑杆拇指/头像等静态场景不再全速重绘
    LaunchedEffect(blinking) {
        if (!blinking) return@LaunchedEffect
        while (true) {
            delay(2600L + Random.nextLong(1500L))
            eyeScale.animateTo(0.06f, tween(70))
            eyeScale.animateTo(1f, tween(100))
        }
    }
    val jump = remember { Animatable(0f) }
    val squash = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun hop() {
        scope.launch {
            launch {
                jump.animateTo(-1f, tween(190, easing = FastOutSlowInEasing))
                jump.animateTo(
                    0f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                squash.animateTo(1f, tween(110))
                squash.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = 380f))
            }
        }
    }

    // 闲时随机小跳，让角色一直活着
    LaunchedEffect(idleHop) {
        if (!idleHop) return@LaunchedEffect
        while (true) {
            delay(8000L + Random.nextLong(7000L))
            hop()
        }
    }

    Canvas(
        modifier = modifier
            .size(size)
            .then(
                if (onTap != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            hop()
                            onTap.invoke()
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        val s = this.size.minDimension / 100f
        val breathK = if (breathing) 1f + 0.02f * sin(breath.value * PI.toFloat()) else 1f
        val jy = jump.value * 16f * s
        val sqx = 1f + 0.10f * squash.value
        val sqy = 1f - 0.14f * squash.value
        val wig = if (celebrate) wiggle.value else 0f

        withTransform({
            translate(left = 0f, top = jy)
            rotate(degrees = wig, pivot = Offset(50f * s, 92f * s))
            scale(sqx * breathK, sqy * breathK, pivot = Offset(50f * s, 94f * s))
        }) {
            drawPoopBody(s, palette, eyeScale.value, mood, celebrate)
        }
    }
}

/** 三层漩涡主体（100×100 单位空间） */
internal fun swirlBody(s: Float, top: Float = 0f, scale: Float = 1f): Path {
    fun x(v: Float) = (50f + (v - 50f) * scale) * s
    fun y(v: Float) = (top + (v - 12f) * scale + 12f) * s
    return Path().apply {
        // 底层
        moveTo(x(16f), y(87f))
        cubicTo(x(16f), y(74f), x(25f), y(67.5f), x(35f), y(66f))
        cubicTo(x(40f), y(65.2f), x(45f), y(64.8f), x(50f), y(64.8f))
        cubicTo(x(55f), y(64.8f), x(60f), y(65.2f), x(65f), y(66f))
        cubicTo(x(75f), y(67.5f), x(84f), y(74f), x(84f), y(87f))
        cubicTo(x(84f), y(93.5f), x(68f), y(96.5f), x(50f), y(96.5f))
        cubicTo(x(32f), y(96.5f), x(16f), y(93.5f), x(16f), y(87f))
        close()
        // 中层
        moveTo(x(26f), y(63f))
        cubicTo(x(26f), y(51f), x(34f), y(45f), x(43f), y(43.5f))
        cubicTo(x(47.5f), y(42.8f), x(52.5f), y(42.8f), x(57f), y(43.5f))
        cubicTo(x(66f), y(45f), x(74f), y(51f), x(74f), y(63f))
        cubicTo(x(74f), y(68.5f), x(62f), y(71.5f), x(50f), y(71.5f))
        cubicTo(x(38f), y(71.5f), x(26f), y(68.5f), x(26f), y(63f))
        close()
        // 顶层
        moveTo(x(35f), y(42f))
        cubicTo(x(35f), y(32f), x(40f), y(25.5f), x(47f), y(24.5f))
        cubicTo(x(49f), y(24.2f), x(51f), y(24.2f), x(53f), y(24.5f))
        cubicTo(x(60f), y(25.5f), x(65f), y(32f), x(65f), y(42f))
        cubicTo(x(65f), y(46.5f), x(58f), y(49f), x(50f), y(49f))
        cubicTo(x(42f), y(49f), x(35f), y(46.5f), x(35f), y(42f))
        close()
        // 顶尖球
        addOval(Rect(x(46.5f), y(12.5f), x(57.5f), y(23.5f)))
    }
}

private fun DrawScope.drawPoopBody(
    s: Float,
    palette: AppPalette,
    eyeK: Float,
    mood: Int,
    celebrate: Boolean
) {
    val poop = palette.poop
    val body = swirlBody(s)
    drawPath(body, poop)

    // 卷曲的小尾巴
    val tail = Path().apply {
        moveTo(55f * s, 14.5f * s)
        cubicTo(58f * s, 9.5f * s, 64f * s, 8.5f * s, 67f * s, 12.5f * s)
    }
    drawPath(tail, poop, style = Stroke(4.5f * s, cap = StrokeCap.Round))

    clipPath(body) {
        // 底部阴影
        drawOval(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, palette.poopDark.copy(alpha = 0.30f))
            ),
            topLeft = Offset(12f * s, 78f * s),
            size = Size(76f * s, 22f * s)
        )
        // 层间投影
        drawOval(
            palette.poopDark.copy(alpha = 0.14f),
            topLeft = Offset(30f * s, 65.5f * s),
            size = Size(40f * s, 5f * s)
        )
        drawOval(
            palette.poopDark.copy(alpha = 0.12f),
            topLeft = Offset(37f * s, 45.5f * s),
            size = Size(26f * s, 4f * s)
        )
        // 高光
        drawOval(
            palette.poopLight.copy(alpha = 0.5f),
            topLeft = Offset(38f * s, 30f * s),
            size = Size(7f * s, 12f * s)
        )
        drawOval(
            palette.poopLight.copy(alpha = 0.35f),
            topLeft = Offset(28f * s, 52f * s),
            size = Size(6f * s, 11f * s)
        )
    }

    // 眼睛（可眨）
    val eyeY = 77f * s
    drawOval(
        FaceDark,
        topLeft = Offset(37.3f * s, eyeY - 2.7f * s * eyeK),
        size = Size(5.4f * s, 5.4f * s * eyeK)
    )
    drawOval(
        FaceDark,
        topLeft = Offset(57.3f * s, eyeY - 2.7f * s * eyeK),
        size = Size(5.4f * s, 5.4f * s * eyeK)
    )
    // 腮红
    drawOval(
        Blush.copy(alpha = 0.5f),
        topLeft = Offset(27.5f * s, 81.5f * s),
        size = Size(8f * s, 4.6f * s)
    )
    drawOval(
        Blush.copy(alpha = 0.5f),
        topLeft = Offset(64.5f * s, 81.5f * s),
        size = Size(8f * s, 4.6f * s)
    )

    // 嘴
    when {
        celebrate -> {
            drawOval(FaceDark, topLeft = Offset(44.5f * s, 80f * s), size = Size(11f * s, 8f * s))
            drawOval(Color(0xFFE8938A), topLeft = Offset(47f * s, 84f * s), size = Size(6f * s, 3.2f * s))
        }
        mood <= 0 -> {
            drawArc(
                FaceDark, 30f, 120f, false,
                topLeft = Offset(44.5f * s, 76f * s), size = Size(11f * s, 11f * s),
                style = Stroke(2.2f * s, cap = StrokeCap.Round)
            )
        }
        mood == 1 -> {
            drawArc(
                FaceDark, 45f, 90f, false,
                topLeft = Offset(45.5f * s, 77.5f * s), size = Size(9f * s, 9f * s),
                style = Stroke(2.2f * s, cap = StrokeCap.Round)
            )
        }
        mood == 2 -> {
            drawLine(
                FaceDark,
                Offset(45f * s, 83.5f * s), Offset(55f * s, 83.5f * s),
                strokeWidth = 2.2f * s, cap = StrokeCap.Round
            )
        }
        else -> {
            drawArc(
                FaceDark, 210f, 120f, false,
                topLeft = Offset(44.5f * s, 79f * s), size = Size(11f * s, 11f * s),
                style = Stroke(2.2f * s, cap = StrokeCap.Round)
            )
            drawLine(
                FaceDark,
                Offset(36f * s, 71f * s), Offset(43f * s, 73.5f * s),
                strokeWidth = 2f * s, cap = StrokeCap.Round
            )
            drawLine(
                FaceDark,
                Offset(57f * s, 73.5f * s), Offset(64f * s, 71f * s),
                strokeWidth = 2f * s, cap = StrokeCap.Round
            )
            if (mood >= 4) {
                drawOval(
                    Color(0xFF8FB6D9),
                    topLeft = Offset(60.5f * s, 81.5f * s),
                    size = Size(4f * s, 5.4f * s)
                )
            }
        }
    }
}
