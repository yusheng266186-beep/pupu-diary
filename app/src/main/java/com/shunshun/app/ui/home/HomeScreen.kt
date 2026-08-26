package com.shunshun.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shunshun.app.data.PoopRecord
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
import com.shunshun.app.ui.components.formatTime
import com.shunshun.app.ui.components.dayKey
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun HomeScreen(records: List<PoopRecord>, onRecord: () -> Unit) {
    val now = remember { java.util.Calendar.getInstance() }
    val minuteOfDay = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
    val greeting = when (minuteOfDay) { in 300..689 -> "早上好呀～"; in 690..839 -> "中午好呀～"; in 840..1109 -> "下午好呀～"; else -> "晚上好呀～" }
    val todayCount = records.count { dayKey(it.timestamp) == dayKey(now.timeInMillis) }
    val transition = rememberInfiniteTransition(label = "home-breath")
    val bob by transition.animateFloat(.985f, 1.015f, infiniteRepeatable(tween(2_300, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "character-bob")
    var bubble by remember { mutableStateOf(false) }
    var bubbleText by remember { mutableStateOf("今天也要顺顺利利～") }
    val bubbleLines = remember { listOf("今天也要顺顺利利～", "记得多喝水呀！", "肚肚舒服最重要～", "今天吃蔬菜了吗？", "保持好心情～") }
    LaunchedEffect(bubble) { if (bubble) { delay(1_700L); bubble = false } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(greeting, color = com.shunshun.app.ui.Ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Text("今天记录了吗？", color = Muted, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(top = 5.dp))
            Text("今天记录 $todayCount 次", color = SageDark, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            Spacer(Modifier.height(17.dp))
            Box(Modifier.fillMaxWidth().height(370.dp).background(Meadow.copy(alpha = .62f), RoundedCornerShape(31.dp)), contentAlignment = Alignment.BottomCenter) {
                HomeGarden()
                Box(Modifier.padding(bottom = 43.dp).scale(bob).clickable { bubbleText = bubbleLines[Random.nextInt(bubbleLines.size)]; bubble = true }) { PoopMascot(112.dp, MascotMood.Happy) }
                AnimatedVisibility(visible = bubble, modifier = Modifier.align(Alignment.Center).offset(y = (-72).dp)) { Text(bubbleText, color = BrownDark, fontSize = 12.sp, modifier = Modifier.background(Surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 9.dp)) }
                Text("顺顺，今天也要轻松一点", color = SageDark, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 21.dp))
            }
            Spacer(Modifier.height(17.dp)); PrimaryButton("记录便便", onRecord); Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(18.dp)).border(1.dp, Line.copy(alpha = .65f), RoundedCornerShape(18.dp)).padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (records.isEmpty()) "完成第一次记录，就能看到自己的小规律" else "最近一次：${formatTime(records.first().timestamp)} · ${records.first().stoolType}型", color = Muted, fontSize = 12.sp); Spacer(Modifier.weight(1f)); Text("✿", color = Peach, fontSize = 18.sp)
            }
            Spacer(Modifier.height(22.dp))
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
