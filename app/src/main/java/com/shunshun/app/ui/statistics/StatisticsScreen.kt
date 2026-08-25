package com.shunshun.app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.BlueMist
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Ink
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Lilac
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Peach
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.Yellow
import com.shunshun.app.ui.components.EmptyState
import com.shunshun.app.ui.components.MascotMood
import com.shunshun.app.ui.components.SectionCard
import com.shunshun.app.ui.components.SegmentedControl
import com.shunshun.app.ui.components.dayKey
import com.shunshun.app.ui.components.streak

@Composable
fun StatisticsScreen(records: List<PoopRecord>) {
    var range by rememberSaveable { mutableStateOf("周") }
    val average = if (records.isEmpty()) 0 else records.map { it.smoothness }.average().toInt()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Spacer(Modifier.height(22.dp)); Text("统计分析", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold); Text("把每天的小变化，变成看得见的趋势", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)); Spacer(Modifier.height(11.dp)); SegmentedControl(range, listOf("日", "周", "月", "年")) { range = it } }
        item { SectionCard("最近状态") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Metric("记录总数", records.size.toString()); Metric("平均顺畅度", "$average%"); Metric("连续记录", if (records.isEmpty()) "0 天" else "${streak(records)} 天") } } }
        item { SectionCard("排便节奏") { RhythmChart(records) } }
        item { SectionCard("便便类型分布") { TypeDonut(records) } }
        item { TrendRow(records) }
        item { Spacer(Modifier.height(22.dp)) }
    }
}

@Composable private fun Metric(label: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = SageDark, fontSize = 26.sp, fontWeight = FontWeight.SemiBold); Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp)) } }

@Composable
private fun RhythmChart(records: List<PoopRecord>) {
    if (records.isEmpty()) { EmptyState("再记录几次，我就能帮你发现规律啦。", "你的每一次记录，都会变成温柔的趋势线。", MascotMood.Water); return }
    val points = records.take(7).reversed().map { it.smoothness }
    Canvas(Modifier.fillMaxWidth().height(190.dp).padding(horizontal = 6.dp)) { val chartW = size.width; val chartH = size.height - 28f; val slot = chartW / points.size.coerceAtLeast(1); val barW = (slot * .42f).coerceAtLeast(18f); for (i in 0..3) { val y = chartH * i / 3f; drawLine(Line, Offset(0f, y), Offset(chartW, y), 1f) }; points.forEachIndexed { index, point -> val barH = chartH * point / 100f; val x = slot * index + (slot - barW) / 2f; drawRoundRect(Sage.copy(alpha = .88f), topLeft = Offset(x, chartH - barH), size = Size(barW, barH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)); drawCircle(Color.White.copy(alpha = .55f), 2.5f, Offset(x + barW / 2f, chartH - barH + 10f)) } }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { points.indices.forEach { Text("${it + 1}", color = Muted, fontSize = 10.sp) } }
}

@Composable
private fun TypeDonut(records: List<PoopRecord>) { if (records.isEmpty()) { Text("有了几次记录后，这里会显示你的类型分布。", color = Muted, fontSize = 13.sp); return }; val counts = (1..7).map { type -> records.count { it.stoolType == type } }; val colors = listOf(SageDark, Sage, SageLight, Yellow, Peach, Lilac, BlueMist); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(155.dp), contentAlignment = Alignment.Center) { Canvas(Modifier.fillMaxSize().padding(14.dp)) { val total = counts.sum().coerceAtLeast(1); var start = -90f; counts.forEachIndexed { index, count -> val sweep = 360f * count / total; drawArc(colors[index], start, sweep - 3f, false, style = Stroke(25f)); start += sweep } }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("最常见", color = Muted, fontSize = 11.sp); Text("${counts.indices.maxByOrNull { counts[it] }?.plus(1) ?: 4}型", color = SageDark, fontSize = 25.sp, fontWeight = FontWeight.SemiBold) } }; Spacer(Modifier.width(14.dp)); Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { counts.forEachIndexed { index, count -> Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(9.dp).background(colors[index], CircleShape)); Spacer(Modifier.width(7.dp)); Text("${index + 1}型  $count 次", color = Muted, fontSize = 11.sp) } } } } }

@Composable private fun TrendRow(records: List<PoopRecord>) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { TrendCard(Icons.Outlined.WaterDrop, "平均顺畅度", if (records.isEmpty()) "—" else "${records.map { it.smoothness }.average().toInt()}%", Modifier.weight(1f)); TrendCard(Icons.Outlined.LocalFlorist, "记录天数", "${records.map { dayKey(it.timestamp) }.distinct().size} 天", Modifier.weight(1f)); TrendCard(Icons.Outlined.DirectionsWalk, "生活趋势", if (records.isEmpty()) "等待中" else "在变好", Modifier.weight(1f)) } }

@Composable private fun TrendCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) { Column(modifier.background(Surface, RoundedCornerShape(18.dp)).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(12.dp)) { androidx.compose.material3.Icon(icon, null, tint = SageDark, modifier = Modifier.size(19.dp)); Text(value, color = SageDark, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp)); Text(label, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp)) } }
