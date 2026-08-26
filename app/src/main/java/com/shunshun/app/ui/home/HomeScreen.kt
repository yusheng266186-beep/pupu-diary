package com.shunshun.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.BlueMist
import com.shunshun.app.ui.BrownDark
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Meadow
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Peach
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.Yellow
import com.shunshun.app.ui.components.PoopMascot
import com.shunshun.app.ui.components.MascotMood
import com.shunshun.app.ui.components.PrimaryButton
import com.shunshun.app.ui.components.SectionCard
import com.shunshun.app.ui.components.AppHaptic
import com.shunshun.app.ui.components.appClickable
import com.shunshun.app.ui.components.formatTime
import com.shunshun.app.ui.components.dayKey
import com.shunshun.app.ui.components.rememberAppHaptics
import com.shunshun.app.ui.components.streak
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun HomeScreen(records: List<PoopRecord>, onRecord: () -> Unit) {
    val now = remember { java.util.Calendar.getInstance() }
    val minuteOfDay = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
    val greeting = when (minuteOfDay) { in 300..689 -> "早上好呀～"; in 690..839 -> "中午好呀～"; in 840..1109 -> "下午好呀～"; else -> "晚上好呀～" }
    val todayCount = records.count { dayKey(it.timestamp) == dayKey(now.timeInMillis) }
    val currentStreak = streak(records)
    val tips = remember { listOf("喝一杯温水，给肚肚一点温柔的照顾～", "多吃一小份蔬菜，身体会记得这份心意。", "饭后慢慢走几分钟，让今天轻松一点。", "不用追求完美，愿意记录就是很棒的开始。") }
    val todayTip = tips[now.get(java.util.Calendar.DAY_OF_YEAR) % tips.size]
    val transition = rememberInfiniteTransition(label = "home-breath")
    val bob by transition.animateFloat(.985f, 1.015f, infiniteRepeatable(tween(2_300, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "character-bob")
    var bubble by remember { mutableStateOf(false) }
    var bubbleText by remember { mutableStateOf("今天也要顺顺利利～") }
    var tapped by remember { mutableStateOf(false) }
    val tapScale by animateFloatAsState(if (tapped) .88f else 1f, spring(dampingRatio = .58f, stiffness = 620f), label = "mascot-tap")
    val haptics = rememberAppHaptics()
    val bubbleLines = remember { listOf("今天也要顺顺利利～", "记得多喝水呀！", "肚肚舒服最重要～", "今天吃蔬菜了吗？", "保持好心情～") }
    LaunchedEffect(bubble) { if (bubble) { delay(1_700L); bubble = false } }
    LaunchedEffect(tapped) { if (tapped) { delay(620L); tapped = false } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(greeting, color = com.shunshun.app.ui.Ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Text("今天记录了吗？", color = Muted, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(top = 5.dp))
            Text("今天记录 $todayCount 次", color = SageDark, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            if (currentStreak > 0) {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(SageLight.copy(alpha = .78f), RoundedCornerShape(16.dp)).padding(horizontal = 11.dp, vertical = 6.dp)) {
                        Text("✦ 连续记录 $currentStreak 天", color = SageDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(17.dp))
            Box(Modifier.fillMaxWidth().height(370.dp).background(Meadow.copy(alpha = .62f), RoundedCornerShape(31.dp)), contentAlignment = Alignment.BottomCenter) {
                HomeGarden()
                Box(Modifier.padding(bottom = 43.dp).scale(bob * tapScale).appClickable {
                    tapped = true
                    bubbleText = bubbleLines[Random.nextInt(bubbleLines.size)]
                    bubble = true
                    haptics(AppHaptic.Light)
                }) { PoopMascot(112.dp, if (tapped) MascotMood.Excited else MascotMood.Happy) }
                SparkleBurst(tapped)
                AnimatedVisibility(visible = bubble, modifier = Modifier.align(Alignment.Center).offset(y = (-72).dp)) { Text(bubbleText, color = BrownDark, fontSize = 12.sp, modifier = Modifier.background(Surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 9.dp)) }
                Text("顺顺，今天也要轻松一点", color = SageDark, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 21.dp))
            }
            Spacer(Modifier.height(17.dp)); PrimaryButton("记录便便", onRecord); Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(18.dp)).border(1.dp, Line.copy(alpha = .65f), RoundedCornerShape(18.dp)).padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (records.isEmpty()) "完成第一次记录，就能看到自己的小规律" else "最近一次：${formatTime(records.first().timestamp)} · ${records.first().stoolType}型", color = Muted, fontSize = 12.sp); Spacer(Modifier.weight(1f)); Text("✿", color = Peach, fontSize = 18.sp)
            }
            Spacer(Modifier.height(14.dp))
            SectionCard("今日小贴士") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TipIcon(now.get(java.util.Calendar.DAY_OF_YEAR) % tips.size)
                    Spacer(Modifier.width(12.dp))
                    Text(todayTip, color = BrownDark, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun BoxScope.SparkleBurst(visible: Boolean) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = .72f, stiffness = 320f),
        label = "sparkle-burst"
    )
    if (progress <= .01f) return
    Canvas(
        Modifier.align(Alignment.Center).offset(y = (-8).dp).size(146.dp)
            .graphicsLayer {
                alpha = progress
                val scale = .72f + progress * .28f
                scaleX = scale
                scaleY = scale
                translationY = 18f * (1f - progress)
            }
    ) {
        val alpha = progress.coerceIn(0f, 1f)
        drawSparkle(Offset(size.width * .20f, size.height * .43f), size.minDimension * .055f, Peach.copy(alpha = alpha))
        drawFlowerSparkle(Offset(size.width * .50f, size.height * .25f), size.minDimension * .048f, Yellow.copy(alpha = alpha))
        drawSparkle(Offset(size.width * .81f, size.height * .46f), size.minDimension * .048f, SageDark.copy(alpha = alpha))
    }
}

private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    drawLine(color, Offset(center.x - radius * 1.8f, center.y), Offset(center.x + radius * 1.8f, center.y), radius * .38f, StrokeCap.Round)
    drawLine(color, Offset(center.x, center.y - radius * 1.8f), Offset(center.x, center.y + radius * 1.8f), radius * .38f, StrokeCap.Round)
    drawLine(color.copy(alpha = .62f), Offset(center.x - radius * 1.05f, center.y - radius * 1.05f), Offset(center.x + radius * 1.05f, center.y + radius * 1.05f), radius * .28f, StrokeCap.Round)
    drawLine(color.copy(alpha = .62f), Offset(center.x + radius * 1.05f, center.y - radius * 1.05f), Offset(center.x - radius * 1.05f, center.y + radius * 1.05f), radius * .28f, StrokeCap.Round)
}

private fun DrawScope.drawFlowerSparkle(center: Offset, radius: Float, color: Color) {
    repeat(5) { index ->
        val angle = (index * 72f - 90f) * (Math.PI / 180f)
        val petal = Offset(center.x + kotlin.math.cos(angle).toFloat() * radius * .72f, center.y + kotlin.math.sin(angle).toFloat() * radius * .72f)
        drawCircle(color.copy(alpha = .72f), radius * .58f, petal)
    }
    drawCircle(Peach.copy(alpha = .94f), radius * .25f, center)
}

@Composable
private fun TipIcon(kind: Int) {
    Canvas(Modifier.size(38.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        when (kind) {
            0 -> {
                drawCircle(BlueMist.copy(alpha = .58f), 18f, center)
                drawOval(SageDark.copy(alpha = .72f), topLeft = Offset(center.x - 5f, center.y - 10f), size = Size(10f, 18f))
                drawLine(SageDark, Offset(center.x, center.y + 8f), Offset(center.x + 4f, center.y - 3f), 2f, StrokeCap.Round)
            }
            1 -> {
                drawCircle(SageLight, 18f, center)
                drawLine(SageDark, Offset(center.x, center.y + 11f), Offset(center.x, center.y - 10f), 2.4f, StrokeCap.Round)
                drawOval(Sage.copy(alpha = .85f), topLeft = Offset(center.x - 13f, center.y - 9f), size = Size(13f, 8f))
                drawOval(SageDark.copy(alpha = .72f), topLeft = Offset(center.x + 1f, center.y - 1f), size = Size(13f, 8f))
            }
            2 -> {
                drawCircle(Yellow.copy(alpha = .74f), 11f, center)
                for (i in 0 until 8) {
                    val angle = i * (Math.PI / 4.0)
                    val start = Offset(center.x + kotlin.math.cos(angle).toFloat() * 14f, center.y + kotlin.math.sin(angle).toFloat() * 14f)
                    val end = Offset(center.x + kotlin.math.cos(angle).toFloat() * 18f, center.y + kotlin.math.sin(angle).toFloat() * 18f)
                    drawLine(Peach, start, end, 2f, StrokeCap.Round)
                }
            }
            else -> {
                drawCircle(Peach.copy(alpha = .68f), 18f, center)
                drawLine(BrownDark.copy(alpha = .72f), Offset(center.x - 10f, center.y + 4f), Offset(center.x + 10f, center.y + 4f), 2f, StrokeCap.Round)
                drawCircle(BrownDark.copy(alpha = .72f), 2.5f, Offset(center.x - 7f, center.y - 3f))
                drawCircle(BrownDark.copy(alpha = .72f), 2.5f, Offset(center.x + 7f, center.y - 3f))
            }
        }
    }
}

@Composable
private fun HomeGarden() {
    val transition = rememberInfiniteTransition(label = "garden-motion")
    val sunBob by transition.animateFloat(0f, 3f, infiniteRepeatable(tween(4_800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sun-bob")
    val cloudDrift by transition.animateFloat(-4f, 4f, infiniteRepeatable(tween(10_000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "cloud-drift")
    val leftLeaf by transition.animateFloat(-1.5f, 1.5f, infiniteRepeatable(tween(3_600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "left-leaf")
    val rightLeaf by transition.animateFloat(1.5f, -1.5f, infiniteRepeatable(tween(4_300, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "right-leaf")
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawCircle(Yellow.copy(alpha = .94f), 29f, Offset(w - 45f, 46f + sunBob))
        drawCloud(45f + cloudDrift, 65f, 1f)
        drawOval(Color(0xFFDCE8C5), topLeft = Offset(-w * .08f, h * .53f), size = Size(w * 1.16f, h * .57f)); drawOval(Color(0xFFC9DDAA), topLeft = Offset(-w * .14f, h * .74f), size = Size(w * 1.28f, h * .42f))
        withTransform({ rotate(leftLeaf, pivot = Offset(22f, h - 6f)) }) { sidePlant(22f, h - 6f, -1f) }
        withTransform({ rotate(rightLeaf, pivot = Offset(w - 22f, h - 10f)) }) { sidePlant(w - 22f, h - 10f, 1f) }
        smallPlant(57f, h - 18f, -1f); smallPlant(w - 57f, h - 21f, 1f)
        for (i in 0..15) { val x = (i * 43f) % w; drawLine(SageDark.copy(alpha = .38f), Offset(x, h - 16f), Offset(x + if (i % 2 == 0) 3f else -2f, h - 27f), 1.6f, StrokeCap.Round) }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(x: Float, y: Float, scale: Float) {
    drawCircle(Color.White.copy(alpha = .64f), 21f * scale, Offset(x, y))
    drawCircle(Color.White.copy(alpha = .64f), 17f * scale, Offset(x + 21f * scale, y))
    drawCircle(Color.White.copy(alpha = .64f), 13f * scale, Offset(x + 36f * scale, y))
    drawLine(SageDark.copy(alpha = .18f), Offset(x - 11f * scale, y + 7f * scale), Offset(x + 46f * scale, y + 7f * scale), 1.6f, StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.sidePlant(x: Float, y: Float, direction: Float) {
    val stem = SageDark.copy(alpha = .56f)
    drawLine(stem, Offset(x, y), Offset(x + direction * 42f, y - 205f), 4f, StrokeCap.Round)
    drawLine(stem, Offset(x + direction * 17f, y - 85f), Offset(x + direction * 76f, y - 125f), 3f, StrokeCap.Round)
    drawLine(stem, Offset(x + direction * 31f, y - 145f), Offset(x + direction * 84f, y - 185f), 3f, StrokeCap.Round)
    leaf(x + direction * 44f, y - 211f, direction * -18f, Sage.copy(alpha = .82f))
    leaf(x + direction * 73f, y - 129f, direction * 21f, SageLight.copy(alpha = .92f))
    leaf(x + direction * 82f, y - 190f, direction * -24f, Sage.copy(alpha = .72f))
    leaf(x + direction * 22f, y - 101f, direction * 20f, SageLight.copy(alpha = .86f))
    flower(x + direction * 64f, y - 46f, Peach)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.smallPlant(x: Float, y: Float, direction: Float) {
    drawLine(SageDark.copy(alpha = .55f), Offset(x, y), Offset(x + direction * 17f, y - 83f), 2.5f, StrokeCap.Round)
    leaf(x + direction * 16f, y - 78f, direction * -20f, Sage.copy(alpha = .72f))
    leaf(x + direction * 4f, y - 48f, direction * 18f, SageLight.copy(alpha = .8f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.leaf(x: Float, y: Float, rotation: Float, color: Color) {
    withTransform({ rotate(rotation, pivot = Offset(x, y)) }) {
        drawOval(color, topLeft = Offset(x - 24f, y - 10f), size = Size(48f, 20f))
        drawOval(SageDark.copy(alpha = .20f), topLeft = Offset(x - 24f, y - 10f), size = Size(48f, 20f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.6f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.flower(x: Float, y: Float, color: Color) {
    drawCircle(color.copy(alpha = .82f), 7f, Offset(x - 6f, y))
    drawCircle(color.copy(alpha = .72f), 7f, Offset(x + 6f, y))
    drawCircle(color.copy(alpha = .72f), 7f, Offset(x, y - 6f))
    drawCircle(color.copy(alpha = .72f), 7f, Offset(x, y + 6f))
    drawCircle(Yellow, 3.5f, Offset(x, y))
}
