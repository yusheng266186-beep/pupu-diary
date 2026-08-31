package com.pupu.diary.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.ui.AppState
import com.pupu.diary.ui.components.ConfettiBurst
import com.pupu.diary.ui.components.PoopCharacter
import com.pupu.diary.ui.components.PrimaryButton
import com.pupu.diary.ui.components.Tab
import com.pupu.diary.ui.theme.LocalAppPalette
import com.pupu.diary.util.toStartMs
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun HomeScreen(state: AppState, onGoTab: (Tab) -> Unit, onGoTips: () -> Unit) {
    val palette = LocalAppPalette.current
    val todayStart = remember { LocalDate.now().toStartMs() }
    val todayCount = state.records.count { it.time >= todayStart }
    var heartTrigger by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    // 入场错峰动画进度
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(720, easing = FastOutSlowInEasing))
    }

    // 时段问候（设置了昵称则带上称呼）
    val greeting = remember(state.userName) {
        val base = when (java.time.LocalTime.now().hour) {
            in 5..11 -> "早上好呀"
            in 12..13 -> "中午好呀"
            in 14..18 -> "下午好呀"
            else -> "晚上好呀"
        }
        if (state.userName.isBlank()) "$base～" else "$base，${state.userName}～"
    }

    // 点击角色：爱心迸发 + 气泡对话
    val bubblePhrases = remember {
        listOf(
            "今天也要顺顺利利～", "噗噗～", "记得多喝水哦",
            "有你真好", "今天状态怎么样？", "轻轻记录，轻松每一天"
        )
    }
    var bubbleShow by remember { mutableStateOf(false) }
    val bubblePhrase = bubblePhrases[(heartTrigger - 1).coerceAtLeast(0) % bubblePhrases.size]
    LaunchedEffect(heartTrigger) {
        if (heartTrigger > 0) {
            bubbleShow = true
            delay(2200)
            bubbleShow = false
        }
    }

    fun frac(from: Float, to: Float) =
        ((enter.value - from) / (to - from)).coerceIn(0f, 1f)

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                greeting,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textMain,
                modifier = Modifier.graphicsLayer {
                    val f = frac(0.05f, 0.4f)
                    alpha = f
                    translationY = (1f - f) * 26f
                }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "今天记录了吗？",
                fontSize = 15.sp,
                color = palette.textSub,
                modifier = Modifier.graphicsLayer {
                    val f = frac(0.15f, 0.5f)
                    alpha = f
                    translationY = (1f - f) * 22f
                }
            )
            Spacer(Modifier.height(10.dp))
            AnimatedContent(
                targetState = todayCount,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "countText"
            ) { n ->
                Text(
                    "记录 $n 次",
                    fontSize = 13.sp,
                    color = palette.textSub,
                    modifier = Modifier.graphicsLayer {
                        alpha = frac(0.25f, 0.55f)
                    }
                )
            }
            Spacer(Modifier.height(14.dp))

            // 治愈场景卡（对齐参考稿：按钮坐在草丘上，贴士入口为左上角小徽章）
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        val f = frac(0.35f, 0.85f)
                        alpha = f
                        val sc = 0.96f + 0.04f * f
                        scaleX = sc
                        scaleY = sc
                    }
                    .clip(RoundedCornerShape(28.dp))
            ) {
                HomeScene(onCharacterTap = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    heartTrigger++
                })
                // 小贴士入口（左上角半透明小徽章）
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.78f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onGoTips() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LeafIcon(tint = palette.primary)
                        Spacer(Modifier.width(5.dp))
                        Text("小贴士", fontSize = 11.sp, color = palette.textSub)
                    }
                }
                // 记录按钮：坐在草丘上（参考稿位置）
                PrimaryButton(
                    text = "记录便便",
                    onClick = { onGoTab(Tab.RECORD) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.62f)
                        .padding(bottom = 20.dp),
                    height = 52.dp
                )
                // 角色头顶的气泡对话（置于场景卡顶部，小屏也不会被裁剪）
                androidx.compose.animation.AnimatedVisibility(
                    visible = bubbleShow,
                    enter = scaleIn(
                        initialScale = 0.6f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 420f)
                    ) + fadeIn(tween(120)),
                    exit = fadeOut(tween(180)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = palette.card,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            bubblePhrase,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.primaryDeep,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                ConfettiBurst(
                    trigger = heartTrigger,
                    heart = true,
                    count = 10,
                    originFrac = Offset(0.5f, 0.45f)
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

/** 小叶子图标（贴士徽章用） */
@Composable
private fun LeafIcon(tint: Color) {
    Canvas(Modifier.size(12.dp)) {
        rotate(degrees = -32f, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawOval(
                tint,
                topLeft = Offset(size.width * 0.12f, size.height * 0.22f),
                size = Size(size.width * 0.76f, size.height * 0.4f)
            )
        }
        drawLine(
            tint.copy(alpha = 0.6f),
            Offset(size.width * 0.2f, size.height * 0.8f),
            Offset(size.width * 0.8f, size.height * 0.2f),
            strokeWidth = size.width * 0.09f,
            cap = StrokeCap.Round
        )
    }
}

/** 首页场景：天空 + 太阳 + 云 + 草地 + 便便主角 */
@Composable
private fun HomeScene(onCharacterTap: () -> Unit) {
    val palette = LocalAppPalette.current
    val sunT = rememberInfiniteTransition(label = "sun")
    val sunAngle by sunT.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "sunA"
    )
    val cloudT = rememberInfiniteTransition(label = "cloud")
    val cloudX by cloudT.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cloudX"
    )
    val grassT = rememberInfiniteTransition(label = "grass")
    val grass by grassT.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "grassA"
    )
    val sparkT = rememberInfiniteTransition(label = "spark")
    val spark by sparkT.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sparkA"
    )

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                Brush.verticalGradient(
                    listOf(palette.bg, lerp(palette.bg, palette.primarySoft, 0.55f))
                )
            )

            // 太阳与光线
            val sunC = Offset(w * 0.78f, h * 0.15f)
            val sunR = w * 0.055f
            repeat(8) { i ->
                rotate(degrees = sunAngle + i * 45f, pivot = sunC) {
                    drawLine(
                        palette.accent.copy(alpha = 0.65f),
                        Offset(sunC.x, sunC.y - sunR * 1.55f),
                        Offset(sunC.x, sunC.y - sunR * 1.95f),
                        strokeWidth = w * 0.012f,
                        cap = StrokeCap.Round
                    )
                }
            }
            drawCircle(palette.accent.copy(alpha = 0.3f), sunR * 1.4f, sunC)
            drawCircle(palette.accent, sunR, sunC)

            // 云
            val cx = w * (0.12f + 0.12f * cloudX)
            val cy = h * 0.12f
            val cloud = Color.White.copy(alpha = 0.85f)
            drawCircle(cloud, w * 0.045f, Offset(cx, cy))
            drawCircle(cloud, w * 0.034f, Offset(cx + w * 0.052f, cy + w * 0.012f))
            drawRoundRect(
                cloud,
                topLeft = Offset(cx - w * 0.048f, cy + w * 0.005f),
                size = Size(w * 0.11f, w * 0.034f),
                cornerRadius = CornerRadius(w * 0.017f, w * 0.017f)
            )

            // 山丘
            drawOval(
                Brush.verticalGradient(listOf(palette.primarySoft, palette.primary)),
                topLeft = Offset(-w * 0.25f, h * 0.60f),
                size = Size(w * 1.5f, h * 0.55f)
            )
            drawOval(
                lerp(palette.primary, palette.primaryDeep, 0.18f).copy(alpha = 0.5f),
                topLeft = Offset(w * 0.42f, h * 0.72f),
                size = Size(w * 0.95f, h * 0.42f)
            )

            // 草地肌理（短促小划线）
            val grassDash = lerp(palette.primary, palette.primaryDeep, 0.3f).copy(alpha = 0.4f)
            listOf(
                listOf(0.22f, 0.74f), listOf(0.34f, 0.79f), listOf(0.58f, 0.77f),
                listOf(0.70f, 0.82f), listOf(0.44f, 0.85f), listOf(0.16f, 0.82f),
                listOf(0.82f, 0.76f), listOf(0.62f, 0.88f)
            ).forEach { (fx, fy) ->
                drawLine(
                    grassDash,
                    Offset(w * fx, h * fy),
                    Offset(w * (fx + 0.025f), h * (fy - 0.008f)),
                    strokeWidth = w * 0.007f,
                    cap = StrokeCap.Round
                )
            }

            // 角落绿植
            plant(w * 0.09f, h * 0.82f, h * 0.20f, w, palette, flip = false)
            plant(w * 0.91f, h * 0.85f, h * 0.16f, w, palette, flip = true)

            // 白色小花
            smallFlower(w * 0.30f, h * 0.73f, w * 0.020f)
            smallFlower(w * 0.68f, h * 0.78f, w * 0.016f)
            smallFlower(w * 0.50f, h * 0.84f, w * 0.013f)

            // 漂浮微光（错峰闪烁的小星星）
            listOf(
                Triple(0.16f, 0.20f, 9f),
                Triple(0.87f, 0.34f, 7f),
                Triple(0.30f, 0.42f, 6f)
            ).forEachIndexed { i, (fx, fy, r) ->
                val phase = if (i % 2 == 0) spark else 1f - spark
                val cx2 = w * fx
                val cy2 = h * fy
                val rr = r * (0.8f + 0.3f * phase)
                val star = Path().apply {
                    moveTo(cx2, cy2 - rr)
                    lineTo(cx2 + rr * 0.3f, cy2 - rr * 0.3f)
                    lineTo(cx2 + rr, cy2)
                    lineTo(cx2 + rr * 0.3f, cy2 + rr * 0.3f)
                    lineTo(cx2, cy2 + rr)
                    lineTo(cx2 - rr * 0.3f, cy2 + rr * 0.3f)
                    lineTo(cx2 - rr, cy2)
                    lineTo(cx2 - rr * 0.3f, cy2 - rr * 0.3f)
                    close()
                }
                drawPath(star, palette.accent.copy(alpha = 0.25f + 0.6f * phase))
            }

            // 草叶
            repeat(11) { i ->
                val bx = w * (0.05f + i * 0.088f)
                val by = h * (0.655f + 0.045f * sin(i * 0.85f))
                rotate(degrees = (grass - 0.5f) * 12f, pivot = Offset(bx, by)) {
                    drawLine(
                        lerp(palette.primary, palette.primaryDeep, 0.4f),
                        Offset(bx, by),
                        Offset(bx + w * 0.004f, by - w * 0.028f),
                        strokeWidth = w * 0.008f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        Box(
            Modifier
                .align(Alignment.Center)
                .padding(bottom = 96.dp)
        ) {
            // 草地上的柔影
            Canvas(Modifier.size(185.dp)) {
                val w = size.width
                val h = size.height
                drawOval(
                    palette.primaryDeep.copy(alpha = 0.16f),
                    topLeft = Offset(w * 0.20f, h * 0.90f),
                    size = Size(w * 0.60f, h * 0.085f)
                )
            }
            PoopCharacter(size = 185.dp, onTap = onCharacterTap, idleHop = true)
        }
    }
}

/** 角落绿植：茎 + 交替叶片 */
private fun DrawScope.plant(
    x: Float,
    baseY: Float,
    height: Float,
    w: Float,
    palette: com.pupu.diary.ui.theme.AppPalette,
    flip: Boolean
) {
    val dir = if (flip) 1f else -1f
    val green = lerp(palette.primary, palette.primaryDeep, 0.35f)
    val tipX = x + dir * w * 0.02f
    val tipY = baseY - height
    drawLine(green, Offset(x, baseY), Offset(tipX, tipY), strokeWidth = w * 0.011f, cap = StrokeCap.Round)
    listOf(0.3f, 0.55f, 0.8f).forEachIndexed { i, f ->
        val lx = x + dir * w * 0.02f * f
        val ly = baseY - height * f
        val side = if (i % 2 == 0) 1f else -1f
        rotate(degrees = dir * side * 38f, pivot = Offset(lx, ly)) {
            drawOval(
                green.copy(alpha = 0.85f),
                topLeft = Offset(lx, ly - w * 0.012f),
                size = Size(w * 0.075f, w * 0.024f)
            )
        }
    }
    rotate(degrees = dir * -12f, pivot = Offset(tipX, tipY)) {
        drawOval(
            green,
            topLeft = Offset(tipX - w * 0.013f, tipY - w * 0.052f),
            size = Size(w * 0.026f, w * 0.052f)
        )
    }
}

/** 白瓣黄心小花 */
private fun DrawScope.smallFlower(cx: Float, cy: Float, r: Float) {
    val center = Offset(cx, cy)
    repeat(5) { i ->
        rotate(degrees = i * 72f, pivot = center) {
            drawCircle(
                Color.White.copy(alpha = 0.95f),
                radius = r * 0.62f,
                center = Offset(cx, cy - r * 1.05f)
            )
        }
    }
    drawCircle(Color(0xFFF2C14E), radius = r * 0.5f, center = center)
}
