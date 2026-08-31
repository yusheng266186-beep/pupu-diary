package com.pupu.diary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.pupu.diary.ui.theme.LocalAppPalette

/** 治愈风软卡片 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    color: Color? = null,
    content: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        color = color ?: palette.card,
        shadowElevation = 3.dp
    ) {
        content()
    }
}

/** App 风格统一弹窗：奶油卡片 + 圆角 + 主题色按钮（替代 Material 默认样式） */
@Composable
fun PupuDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: (@Composable () -> Unit)? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    confirmColor: Color? = null,
    dismissText: String? = null,
    onDismissClick: (() -> Unit)? = null
) {
    val palette = LocalAppPalette.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = palette.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textMain
            )
        },
        text = content?.let { c -> { c() } },
        confirmButton = {
            if (confirmText != null) {
                androidx.compose.material3.TextButton(onClick = { onConfirm?.invoke() }) {
                    Text(
                        confirmText,
                        fontWeight = FontWeight.SemiBold,
                        color = confirmColor ?: palette.primary
                    )
                }
            }
        },
        dismissButton = {
            if (dismissText != null) {
                androidx.compose.material3.TextButton(
                    onClick = { (onDismissClick ?: onDismissRequest).invoke() }
                ) {
                    Text(dismissText, color = palette.textSub)
                }
            }
        }
    )
}

/** 糖果按压主按钮：绿色按钮面坐在深色底边上，按下时面真的沉下去，物理感十足 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp
) {
    val palette = LocalAppPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressY by animateDpAsState(
        targetValue = if (pressed) 4.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "pressY"
    )
    val haptic = LocalHapticFeedback.current
    val faceHeight = height - 4.dp

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(50))
    ) {
        // 深色底边
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(faceHeight)
                .clip(RoundedCornerShape(50))
                .background(lerp(palette.primary, palette.primaryDeep, 0.5f))
        )
        // 按压面
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(faceHeight)
                .graphicsLayer { translationY = pressY.toPx() }
                .clip(RoundedCornerShape(50))
                .background(palette.primary)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

/** 治愈风空状态：角色 + 标题 + 副标题 */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    characterSize: Dp = 62.dp
) {
    val palette = LocalAppPalette.current
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PoopCharacter(size = characterSize, mood = 1)
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textMain
        )
        Spacer(Modifier.height(3.dp))
        Text(
            subtitle,
            fontSize = 12.sp,
            color = palette.textSub,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/** 区块标题（参考稿样式：纯文字半粗体） */
@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    Text(
        title,
        modifier = modifier.padding(vertical = 4.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = palette.textMain
    )
}

/** 返回式页头（标题居中） */
@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(palette.card)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(18.dp)) {
                    val w = this.size.width
                    val h = this.size.height
                    val p = Path().apply {
                        moveTo(w * 0.62f, h * 0.18f)
                        lineTo(w * 0.28f, h * 0.5f)
                        lineTo(w * 0.62f, h * 0.82f)
                    }
                    drawPath(
                        p, palette.textMain,
                        style = Stroke(width = w * 0.14f, cap = StrokeCap.Round)
                    )
                }
            }
        }
        Text(
            title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textMain,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/** 圆形小加号按钮（打卡用） */
@Composable
fun PlusButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.85f else 1f,
        spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "plusScale"
    )
    Box(
        // v4.0.0：视觉 34dp，触控热区扩到 44dp（无障碍标准）
        modifier = modifier
            .size(44.dp)
            .semantics { contentDescription = "打卡 +1" }
            .clickable(interactionSource = interaction, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(34.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .background(palette.primary),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(14.dp)) {
                val w = size.width
                val h = size.height
                drawLine(
                    Color.White, Offset(w / 2f, h * 0.1f), Offset(w / 2f, h * 0.9f),
                    strokeWidth = w * 0.18f, cap = StrokeCap.Round
                )
                drawLine(
                    Color.White, Offset(w * 0.1f, h / 2f), Offset(w * 0.9f, h / 2f),
                    strokeWidth = w * 0.18f, cap = StrokeCap.Round
                )
            }
        }
    }
}

/** 删除撤销条：底部浮层。弹簧入场、快速退出（慢进快出的不对称时机），5 秒倒计时可视化 */
@Composable
fun UndoBar(
    visible: Boolean,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    // v4.0.0：底部细线随 5 秒窗口收缩，用户对"还来不来得及撤销"有视觉预期
    val countdown = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) {
            countdown.snapTo(0f)
            countdown.animateTo(1f, tween(5000, easing = LinearEasing))
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f),
            initialOffsetY = { it }
        ) + fadeIn(tween(120)),
        exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(140)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            shadowElevation = 12.dp,
            color = palette.card
        ) {
            Column {
                Row(
                    Modifier.padding(start = 18.dp, end = 6.dp, top = 5.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("已删除一条记录", fontSize = 13.sp, color = palette.textSub)
                    Text(
                        "撤销",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryDeep,
                        modifier = Modifier
                            .semantics { contentDescription = "撤销刚才的删除" }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onUndo
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
                Canvas(Modifier.fillMaxWidth().height(3.dp)) {
                    drawLine(
                        palette.primary.copy(alpha = 0.55f),
                        Offset(0f, 1.5f),
                        Offset(size.width * (1f - countdown.value), 1.5f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/** 数字步进器（v4.0.0 从"我的"页抽出共享，支持点按与长按连发） */
@Composable
fun PupuStepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit
) {
    val palette = LocalAppPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = palette.textSub)
        Spacer(Modifier.height(6.dp))
        StepCircle(up = true, enabled = value < max) { onChange((value + 1).coerceAtMost(max)) }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(palette.primarySoft)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "%02d".format(value),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primaryDeep
            )
        }
        Spacer(Modifier.height(6.dp))
        StepCircle(up = false, enabled = value > min) { onChange((value - 1).coerceAtLeast(min)) }
    }
}

/** 步进圆钮：按下即响应一次，按住 400ms 后每 80ms 连发（v4.0.0 长按加速） */
@Composable
private fun StepCircle(up: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier
            .size(44.dp)
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = { off ->
                        if (!enabled) return@detectTapGestures
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = off.x - cx
                        val dy = off.y - cy
                        if (dx * dx + dy * dy > cx * cx) return@detectTapGestures
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                        // 400ms 内松手则不连发
                        val held = withTimeoutOrNull(400L) { tryAwaitRelease() } != true
                        if (held) {
                            while (true) {
                                delay(80L)
                                if (withTimeoutOrNull(80L) { tryAwaitRelease() } == true) break
                                onClick()
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(palette.cardAlt.copy(alpha = if (enabled) 1f else 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(14.dp)) {
                val c = if (enabled) palette.textMain else palette.textSub.copy(alpha = 0.4f)
                val p = Path().apply {
                    if (up) {
                        moveTo(size.width * 0.25f, size.height * 0.62f)
                        lineTo(size.width * 0.5f, size.height * 0.32f)
                        lineTo(size.width * 0.75f, size.height * 0.62f)
                    } else {
                        moveTo(size.width * 0.25f, size.height * 0.38f)
                        lineTo(size.width * 0.5f, size.height * 0.68f)
                        lineTo(size.width * 0.75f, size.height * 0.38f)
                    }
                }
                drawPath(p, c, style = Stroke(size.width * 0.2f, cap = StrokeCap.Round))
            }
        }
    }
}

/** 任意日期+时间选择（v4.0.0 补记升级：不再只有 5 个固定时间点） */
@Composable
fun DateTimeDialog(
    title: String,
    initialMs: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val palette = LocalAppPalette.current
    val zone = java.time.ZoneId.systemDefault()
    val init = java.time.Instant.ofEpochMilli(initialMs).atZone(zone)
    var year by remember { mutableIntStateOf(init.year) }
    var month by remember { mutableIntStateOf(init.monthValue) }
    var day by remember { mutableIntStateOf(init.dayOfMonth) }
    var hour by remember { mutableIntStateOf(init.hour) }
    var minute by remember { mutableIntStateOf(init.minute) }
    val thisYear = java.time.LocalDate.now().year

    fun clampedMs(): Long {
        val y = year.coerceIn(2000, thisYear)
        val m = month.coerceIn(1, 12)
        val maxD = java.time.LocalDate.of(y, m, 1).lengthOfMonth()
        val d = day.coerceIn(1, maxD)
        return java.time.LocalDateTime.of(y, m, d, hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            .atZone(zone).toInstant().toEpochMilli()
            .coerceAtMost(System.currentTimeMillis())
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = palette.card) {
            Column(Modifier.padding(22.dp)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PupuStepper("年", (year - 2000).coerceIn(0, 99), 0, thisYear - 2000) { year = 2000 + it }
                    Spacer(Modifier.width(8.dp))
                    PupuStepper("月", month, 1, 12) { month = it }
                    Spacer(Modifier.width(8.dp))
                    PupuStepper("日", day, 1, 31) { day = it }
                    Spacer(Modifier.width(14.dp))
                    PupuStepper("时", hour, 0, 23) { hour = it }
                    Text(":", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = palette.textSub)
                    PupuStepper("分", minute, 0, 59) { minute = it }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消", color = palette.textSub) }
                    TextButton(onClick = { onConfirm(clampedMs()) }) {
                        Text("确定", fontWeight = FontWeight.SemiBold, color = palette.primary)
                    }
                }
            }
        }
    }
}
