package com.pupu.diary.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.data.Bristol
import com.pupu.diary.data.PoopColors
import com.pupu.diary.ui.AppState
import com.pupu.diary.ui.components.BackHeader
import com.pupu.diary.ui.components.BristolIcon
import com.pupu.diary.ui.components.PoopCharacter
import com.pupu.diary.ui.components.PlusButton
import com.pupu.diary.ui.components.SoftCard
import com.pupu.diary.ui.theme.LocalAppPalette

@Composable
fun TipsScreen(state: AppState, onBack: () -> Unit) {
    val palette = LocalAppPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        BackHeader("健康小贴士", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "多喝水、多运动，让身体更轻松～",
                fontSize = 14.sp,
                color = palette.textSub,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            SoftCard(modifier = Modifier.fillMaxWidth(), corner = 28.dp) {
                Box(Modifier.fillMaxWidth().height(170.dp)) {
                    Canvas(Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        drawOval(
                            Brush.verticalGradient(listOf(palette.primarySoft, palette.primary)),
                            topLeft = Offset(-w * 0.2f, h * 0.72f),
                            size = Size(w * 1.4f, h * 0.5f)
                        )
                        // v4.0.0：夜间花朵降透明度，与首页场景同一处理语言
                        val dim = if (palette.isDark) 0.55f else 1f
                        flower(this, w * 0.24f, h * 0.80f, h * 0.26f, w, dim)
                        flower(this, w * 0.80f, h * 0.82f, h * 0.18f, w, dim)
                    }
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        PoopCharacter(size = 108.dp, mood = 0)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("今日建议", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
            Spacer(Modifier.height(8.dp))
            GoalCard(
                0, "喝水", "${state.water} / 8 杯", state.water / 8f,
                canMinus = state.water > 0,
                onPlus = { state.addWater(1) },
                onMinus = { state.addWater(-1) }
            )
            Spacer(Modifier.height(8.dp))
            GoalCard(
                1, "运动", "${state.exercise} / 30 分钟", state.exercise / 30f,
                canMinus = state.exercise > 0,
                onPlus = { state.addExercise(15) },
                onMinus = { state.addExercise(-15) }
            )
            Spacer(Modifier.height(8.dp))
            GoalCard(
                2, "膳食纤维", "${state.fiber} / 25 g", state.fiber / 25f,
                canMinus = state.fiber > 0,
                onPlus = { state.addFiber(5) },
                onMinus = { state.addFiber(-5) }
            )
            Spacer(Modifier.height(16.dp))
            BristolGuide()
            Spacer(Modifier.height(16.dp))
            ColorGuide()
            Spacer(Modifier.height(16.dp))
            TipCard(0, "关于喝水", "每天 1500–2000ml 温水，肠道润滑了，便便才顺畅～")
            Spacer(Modifier.height(8.dp))
            TipCard(2, "关于纤维", "全谷物、蔬菜、水果里的膳食纤维能增加便便体积，让它更松软成形。")
            Spacer(Modifier.height(8.dp))
            TipCard(1, "关于运动", "饭后散步 15 分钟，促进肠道蠕动，便便也会更守时哦。")
            Spacer(Modifier.height(8.dp))
            TipCard(3, "关于作息", "作息规律，肠道才规律。试试每天固定时间去噗噗，身体会记住节奏的。")
            Spacer(Modifier.height(8.dp))
            TipCard(4, "关于饮食", "辛辣和油腻会让便便闹脾气，蔬果和全谷物才是它的好朋友。")
            Spacer(Modifier.height(8.dp))
            TipCard(5, "连续不顺畅？", "晨起一杯温水 + 顺时针揉揉肚子 + 饭后散步，比使劲憋气管用。偶尔借助外物没关系，但别形成依赖；超过一周没动静，建议去看医生。")
            Spacer(Modifier.height(8.dp))
            TipCard(6, "突然变稀？", "受凉、吃坏东西都可能让便便变稀。补温水、饮食清淡，通常 1–2 天会好转；如果持续水样、伴发烧或肚子剧痛，别硬扛，及时就医。")
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** 布里斯托 1–7 型速查：图标 + 名称 + 一句话解读 */
@Composable
private fun BristolGuide() {
    val palette = LocalAppPalette.current
    val notes = listOf(
        "一颗颗硬球，喝水太少啦",
        "结成小块，需要多补水",
        "表面有裂痕，还算不错",
        "光滑柔软，理想状态！",
        "边缘分明，软硬适中",
        "糊状边缘，注意饮食清淡",
        "完全水状，肠胃在报警"
    )
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "便形状速查",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textMain
            )
            Text(
                "对照布里斯托分类，看看今天的状态",
                fontSize = 11.sp,
                color = palette.textSub
            )
            Spacer(Modifier.height(10.dp))
            Bristol.labels.indices.forEach { i ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(palette.primarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        BristolIcon(type = i + 1, size = 24.dp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${i + 1} ${Bristol.label(i + 1)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textMain
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(notes[i], fontSize = 11.sp, color = palette.textSub)
                }
            }
        }
    }
}

private fun flower(
    d: androidx.compose.ui.graphics.drawscope.DrawScope,
    x: Float,
    baseY: Float,
    stemH: Float,
    w: Float,
    dim: Float = 1f
) {
    d.apply {
        val top = Offset(x, baseY - stemH)
        drawLine(
            Color(0xFF6F9A6B).copy(alpha = dim),
            Offset(x, baseY), top,
            strokeWidth = w * 0.012f, cap = StrokeCap.Round
        )
        repeat(5) { i ->
            rotate(degrees = i * 72f, pivot = top) {
                drawCircle(
                    Color(0xFFF2A69B).copy(alpha = dim),
                    radius = w * 0.026f,
                    center = Offset(top.x, top.y - w * 0.036f)
                )
            }
        }
        drawCircle(Color(0xFFF2C14E).copy(alpha = dim), radius = w * 0.018f, center = top)
    }
}

/** 手绘线性小图标（浅绿圆底 + 主题色图形），与全 App 图标语言一致，替代系统 emoji */
@Composable
private fun TipIcon(kind: Int, size: androidx.compose.ui.unit.Dp = 34.dp) {
    val palette = LocalAppPalette.current
    Box(
        Modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(palette.primarySoft),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size * 0.52f)) {
            val w = this.size.width
            val h = this.size.height
            val c = palette.primaryDeep
            when (kind) {
                0 -> { // 水滴
                    val drop = Path().apply {
                        moveTo(w * 0.5f, h * 0.02f)
                        cubicTo(w * 0.72f, h * 0.34f, w * 0.86f, h * 0.5f, w * 0.86f, h * 0.64f)
                        cubicTo(w * 0.86f, h * 0.9f, w * 0.14f, h * 0.9f, w * 0.14f, h * 0.64f)
                        cubicTo(w * 0.14f, h * 0.5f, w * 0.28f, h * 0.34f, w * 0.5f, h * 0.02f)
                        close()
                    }
                    drawPath(drop, c)
                }
                1 -> { // 闪电（运动）
                    val bolt = Path().apply {
                        moveTo(w * 0.58f, h * 0.02f)
                        lineTo(w * 0.2f, h * 0.56f)
                        lineTo(w * 0.44f, h * 0.56f)
                        lineTo(w * 0.38f, h * 0.98f)
                        lineTo(w * 0.8f, h * 0.4f)
                        lineTo(w * 0.54f, h * 0.4f)
                        close()
                    }
                    drawPath(bolt, c)
                }
                2 -> { // 叶子（纤维）
                    rotate(degrees = -30f, pivot = Offset(w * 0.5f, h * 0.55f)) {
                        drawOval(c, topLeft = Offset(w * 0.12f, h * 0.16f), size = Size(w * 0.76f, h * 0.42f))
                    }
                    drawLine(
                        palette.primarySoft,
                        Offset(w * 0.3f, h * 0.72f), Offset(w * 0.7f, h * 0.3f),
                        strokeWidth = w * 0.09f, cap = StrokeCap.Round
                    )
                }
                3 -> { // 月亮（作息）
                    drawCircle(c, radius = w * 0.46f, center = Offset(w * 0.5f, h * 0.52f))
                    drawCircle(
                        palette.primarySoft,
                        radius = w * 0.4f,
                        center = Offset(w * 0.74f, h * 0.34f)
                    )
                }
                4 -> { // 苹果（饮食）
                    drawCircle(c, radius = w * 0.34f, center = Offset(w * 0.5f, h * 0.62f))
                    drawLine(
                        c, Offset(w * 0.5f, h * 0.32f), Offset(w * 0.5f, h * 0.12f),
                        strokeWidth = w * 0.1f, cap = StrokeCap.Round
                    )
                    rotate(degrees = -35f, pivot = Offset(w * 0.56f, h * 0.18f)) {
                        drawOval(c, topLeft = Offset(w * 0.56f, h * 0.1f), size = Size(w * 0.32f, h * 0.15f))
                    }
                }
                5 -> { // 问号（不顺畅）
                    drawArc(
                        c, 90f, 270f, false,
                        topLeft = Offset(w * 0.24f, h * 0.02f),
                        size = Size(w * 0.52f, w * 0.52f),
                        style = Stroke(w * 0.14f, cap = StrokeCap.Round)
                    )
                    drawLine(
                        c, Offset(w * 0.5f, h * 0.54f), Offset(w * 0.5f, h * 0.7f),
                        strokeWidth = w * 0.14f, cap = StrokeCap.Round
                    )
                    drawCircle(c, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.9f))
                }
                else -> { // 水波（变稀）
                    listOf(0.34f, 0.72f).forEach { y ->
                        val p = Path().apply {
                            moveTo(w * 0.04f, h * y)
                            cubicTo(w * 0.3f, h * (y - 0.24f), w * 0.7f, h * (y + 0.24f), w * 0.96f, h * y)
                        }
                        drawPath(p, c, style = Stroke(w * 0.13f, cap = StrokeCap.Round))
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(
    iconKind: Int,
    title: String,
    value: String,
    progress: Float,
    canMinus: Boolean,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    val palette = LocalAppPalette.current
    var pulse by remember { mutableIntStateOf(0) }
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(pulse) {
        if (pulse > 0) {
            pulseScale.animateTo(1.28f, tween(110))
            pulseScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 500f))
        }
    }
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TipIcon(iconKind)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
                    Spacer(Modifier.weight(1f))
                    Text(
                        value,
                        fontSize = 11.sp,
                        color = palette.textSub,
                        modifier = Modifier.graphicsLayer {
                            scaleX = pulseScale.value
                            scaleY = pulseScale.value
                        }
                    )
                }
                Spacer(Modifier.height(7.dp))
                ProgressBar(progress)
            }
            Spacer(Modifier.width(10.dp))
            if (canMinus) {
                MinusButton(onClick = {
                    pulse++
                    onMinus()
                })
                Spacer(Modifier.width(8.dp))
            }
            PlusButton(onClick = {
                pulse++
                onPlus()
            })
        }
    }
}

/** 圆形小减号按钮（打卡回退用） */
@Composable
private fun MinusButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.85f else 1f,
        spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "minusScale"
    )
    Box(
        modifier = modifier
            .size(34.dp)
            .semantics { contentDescription = "回退一次打卡" }
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(palette.cardAlt)
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(12.dp)) {
            drawLine(
                palette.textSub,
                Offset(size.width * 0.1f, size.height / 2f),
                Offset(size.width * 0.9f, size.height / 2f),
                strokeWidth = size.width * 0.2f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float) {
    val palette = LocalAppPalette.current
    val f by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "prog"
    )
    Canvas(Modifier.fillMaxWidth().height(8.dp)) {
        drawRoundRect(
            palette.cardAlt,
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        if (f > 0.01f) {
            drawRoundRect(
                Brush.horizontalGradient(listOf(palette.primary, palette.primaryDeep)),
                size = Size(size.width * f, size.height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

/** 颜色日记速查：色点 + 名称 + 一句话解读（v4.0.0 扩到 8 色，补足黑便与陶土色警示） */
@Composable
private fun ColorGuide() {
    val palette = LocalAppPalette.current
    val notes = listOf(
        "水分偏少，记得多喝水",
        "最理想的状态，继续保持",
        "消化顺畅，正常范围",
        "偏浅了些，可能消化偏快",
        "可能带血！受饮食影响较小，出现一次请留意，持续就医",
        "吃了特别的东西？对照看看",
        "柏油样黑便需警惕消化道出血，持续出现请尽快就医",
        "灰白陶土色可能与胆道有关，反复出现请及时就医"
    )
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "颜色速查",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textMain
            )
            Text(
                "记录时选的颜色代表什么？",
                fontSize = 11.sp,
                color = palette.textSub
            )
            Spacer(Modifier.height(10.dp))
            PoopColors.labels.indices.forEach { i ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(palette.cardAlt)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(PoopColors.color(i))
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            PoopColors.label(i),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.textMain
                        )
                        Text(notes[i], fontSize = 11.sp, color = palette.textSub)
                    }
                }
            }
            Text(
                "多数颜色变化受饮食影响，仅作日常参考；但红色、黑色、灰白色反复出现时别大意，请及时就医。",
                fontSize = 10.sp,
                color = palette.textSub,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TipCard(iconKind: Int, title: String, body: String) {
    val palette = LocalAppPalette.current
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            TipIcon(iconKind, 30.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
                Spacer(Modifier.height(3.dp))
                Text(body, fontSize = 12.sp, color = palette.textSub, lineHeight = 18.sp)
            }
        }
    }
}
