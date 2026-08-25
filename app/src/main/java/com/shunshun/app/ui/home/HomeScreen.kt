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

@Composable
fun HomeScreen(records: List<PoopRecord>, onRecord: () -> Unit) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) { in 5..11 -> "早上好呀～"; in 12..13 -> "中午好呀～"; in 14..18 -> "下午好呀～"; else -> "晚上好呀～" }
    val transition = rememberInfiniteTransition(label = "home-breath")
    val bob by transition.animateFloat(.985f, 1.015f, infiniteRepeatable(tween(2_300, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "character-bob")
    var bubble by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Spacer(Modifier.height(24.dp))
            Text(greeting, color = com.shunshun.app.ui.Ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Text("今天记录了吗？", color = Muted, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(top = 5.dp))
            Text("记录 ${records.size} 次", color = SageDark, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            Spacer(Modifier.height(17.dp))
            Box(Modifier.fillMaxWidth().height(370.dp).background(Meadow.copy(alpha = .62f), RoundedCornerShape(31.dp)), contentAlignment = Alignment.BottomCenter) {
                HomeGarden()
                Box(Modifier.padding(bottom = 43.dp).scale(bob).clickable { bubble = !bubble }) { PoopMascot(112.dp, MascotMood.Happy) }
                AnimatedVisibility(visible = bubble, modifier = Modifier.align(Alignment.Center).offset(y = (-72).dp)) { Text("今天也要顺顺利利～", color = BrownDark, fontSize = 12.sp, modifier = Modifier.background(Surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 9.dp)) }
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
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawCircle(Yellow.copy(alpha = .94f), 29f, Offset(w - 45f, 46f))
        drawCircle(Color.White.copy(alpha = .64f), 21f, Offset(45f, 65f)); drawCircle(Color.White.copy(alpha = .64f), 17f, Offset(66f, 65f)); drawCircle(Color.White.copy(alpha = .64f), 13f, Offset(81f, 65f)); drawLine(SageDark.copy(alpha = .18f), Offset(34f, 72f), Offset(90f, 72f), 1.6f, StrokeCap.Round)
        drawOval(Color(0xFFDCE8C5), topLeft = Offset(-w * .08f, h * .53f), size = Size(w * 1.16f, h * .57f)); drawOval(Color(0xFFC9DDAA), topLeft = Offset(-w * .14f, h * .74f), size = Size(w * 1.28f, h * .42f))
        plant(24f, h - 8f, -1f); plant(w - 25f, h - 12f, 1f); plant(50f, h - 21f, -1f); plant(w - 53f, h - 25f, 1f)
        for (i in 0..15) { val x = (i * 43f) % w; drawLine(SageDark.copy(alpha = .38f), Offset(x, h - 16f), Offset(x + if (i % 2 == 0) 3f else -2f, h - 27f), 1.6f, StrokeCap.Round) }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.plant(x: Float, y: Float, direction: Float) {
    drawLine(SageDark.copy(alpha = .7f), Offset(x, y), Offset(x + direction * 18f, y - 58f), 2.5f, StrokeCap.Round); drawOval(Sage.copy(alpha = .74f), topLeft = Offset(x + direction * 12f, y - 52f), size = Size(18f, 8f)); drawOval(SageLight, topLeft = Offset(x + direction * 2f, y - 36f), size = Size(20f, 9f))
}
