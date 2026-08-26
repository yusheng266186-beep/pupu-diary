package com.shunshun.app.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class ChartBucket(val label: String, val count: Int, val detail: String)

@Composable
fun StatisticsScreen(records: List<PoopRecord>) {
    var range by rememberSaveable { androidx.compose.runtime.mutableStateOf("周") }
    val filtered = records.filterForRange(range)
    val average = if (filtered.isEmpty()) 0 else filtered.map { it.smoothness }.average().toInt()
    val label = when (range) { "日" -> "今日记录"; "月" -> "本月记录"; "年" -> "今年记录"; else -> "本周记录" }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(22.dp))
            Text("统计分析", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
            Text("把每天的小变化，变成看得见的趋势", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(11.dp))
            SegmentedControl(range, listOf("日", "周", "月", "年")) { range = it }
        }
        item {
            SectionCard("最近状态") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric(label, filtered.size.toString())
                    Metric("平均顺畅度", "$average%")
                    Metric("连续记录", if (records.isEmpty()) "0 天" else "${streak(records)} 天")
                }
            }
        }
        item { SectionCard("排便节奏") { RhythmChart(filtered, range) } }
        item { SectionCard("便便类型分布") { TypeDonut(filtered) } }
        item { TrendRow(filtered) }
        item { Spacer(Modifier.height(22.dp)) }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = SageDark, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun RhythmChart(records: List<PoopRecord>, range: String) {
    val buckets = buildBuckets(records, range)
    if (buckets.all { it.count == 0 }) {
        EmptyState("再记录几次，我就能帮你发现规律啦。", "你的每一次记录，都会变成温柔的趋势线。", MascotMood.Water)
        return
    }
    var selected by rememberSaveable(range) { mutableIntStateOf(-1) }
    val maxCount = buckets.maxOf { it.count }.coerceAtLeast(1)
    val heights = buckets.mapIndexed { index, bucket ->
        animateFloatAsState(if (maxCount == 0) 0f else bucket.count.toFloat() / maxCount, tween(560, index * 45), label = "bar-$range-$index").value
    }
    Column {
        if (selected in buckets.indices) {
            Text("${buckets[selected].detail} · ${buckets[selected].count} 次", color = SageDark, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        } else {
            Text("点击柱子查看每天的记录次数", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }
        Canvas(
            Modifier.fillMaxWidth().height(176.dp).padding(horizontal = 6.dp)
                .pointerInput(buckets) { detectTapGestures { point -> selected = ((point.x / size.width) * buckets.size).toInt().coerceIn(0, buckets.lastIndex) } }
        ) {
            val chartW = size.width
            val chartH = size.height - 28f
            val slot = chartW / buckets.size.coerceAtLeast(1)
            val barW = (slot * .48f).coerceAtLeast(16f)
            for (i in 0..3) {
                val y = chartH * i / 3f
                drawLine(Line, Offset(0f, y), Offset(chartW, y), 1f)
            }
            buckets.forEachIndexed { index, bucket ->
                val barH = chartH * heights[index]
                val x = slot * index + (slot - barW) / 2f
                val color = if (selected == index) SageDark else Sage.copy(alpha = .88f)
                drawRoundRect(color, topLeft = Offset(x, chartH - barH), size = Size(barW, barH.coerceAtLeast(4f)), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f))
                if (bucket.count > 0) drawCircle(Color.White.copy(alpha = .58f), 2.5f, Offset(x + barW / 2f, chartH - barH + 10f))
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            buckets.forEach { Text(it.label, color = Muted, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun TypeDonut(records: List<PoopRecord>) {
    if (records.isEmpty()) {
        Text("有了几次记录后，这里会显示你的类型分布。", color = Muted, fontSize = 13.sp)
        return
    }
    val counts = (1..7).map { type -> records.count { it.stoolType == type } }
    val colors = listOf(SageDark, Sage, SageLight, Yellow, Peach, Lilac, BlueMist)
    val commonIndex = counts.indices.maxByOrNull { counts[it] } ?: 0
    val total = counts.sum().coerceAtLeast(1)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(155.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(14.dp)) {
                var start = -90f
                counts.forEachIndexed { index, count ->
                    val sweep = 360f * count / total
                    if (count > 0) drawArc(colors[index], start, (sweep - 3f).coerceAtLeast(1f), false, style = Stroke(25f))
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("最常见", color = Muted, fontSize = 11.sp)
                Text("${commonIndex + 1}型", color = SageDark, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                Text("${counts[commonIndex] * 100 / total}%", color = SageDark, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            counts.forEachIndexed { index, count ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).background(colors[index], CircleShape))
                    Spacer(Modifier.width(7.dp))
                    Text("${index + 1}型  $count 次", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun TrendRow(records: List<PoopRecord>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TrendCard(Icons.Outlined.WaterDrop, "平均顺畅度", if (records.isEmpty()) "—" else "${records.map { it.smoothness }.average().toInt()}%", Modifier.weight(1f))
        TrendCard(Icons.Outlined.LocalFlorist, "记录天数", "${records.map { dayKey(it.timestamp) }.distinct().size} 天", Modifier.weight(1f))
        TrendCard(Icons.Outlined.DirectionsWalk, "生活趋势", if (records.isEmpty()) "等待中" else "在变好", Modifier.weight(1f))
    }
}

@Composable
private fun TrendCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    Column(modifier.background(Surface, RoundedCornerShape(18.dp)).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(12.dp)) {
        Icon(icon, null, tint = SageDark, modifier = Modifier.size(19.dp))
        Text(value, color = SageDark, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
        Text(label, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

private fun List<PoopRecord>.filterForRange(range: String): List<PoopRecord> {
    val now = Calendar.getInstance()
    val start = (now.clone() as Calendar).apply {
        when (range) {
            "日" -> { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            "月" -> { add(Calendar.DAY_OF_YEAR, -29); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            "年" -> { add(Calendar.MONTH, -11); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            else -> { add(Calendar.DAY_OF_YEAR, -6); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        }
    }.timeInMillis
    return filter { it.timestamp >= start && it.timestamp <= now.timeInMillis }
}

private fun buildBuckets(records: List<PoopRecord>, range: String): List<ChartBucket> {
    val now = Calendar.getInstance()
    return when (range) {
        "日" -> listOf(ChartBucket("今天", records.size, "今天"))
        "月" -> (4 downTo 0).map { index ->
            val end = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -index * 7); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }
            val start = (end.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }
            ChartBucket("${5 - index}周", records.count { it.timestamp in start.timeInMillis..end.timeInMillis }, "${SimpleDateFormat("MM/dd", Locale.CHINA).format(start.time)}-${SimpleDateFormat("MM/dd", Locale.CHINA).format(end.time)}")
        }
        "年" -> (11 downTo 0).map { index ->
            val target = (now.clone() as Calendar).apply { add(Calendar.MONTH, -index) }
            val month = target.get(Calendar.MONTH)
            ChartBucket("${month + 1}月", records.count { record -> val date = Calendar.getInstance().apply { timeInMillis = record.timestamp }; date.get(Calendar.YEAR) == target.get(Calendar.YEAR) && date.get(Calendar.MONTH) == month }, "${target.get(Calendar.YEAR)}年${month + 1}月")
        }
        else -> (6 downTo 0).map { index ->
            val target = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -index) }
            val key = dayKey(target.timeInMillis)
            ChartBucket(SimpleDateFormat("d", Locale.CHINA).format(target.time), records.count { dayKey(it.timestamp) == key }, SimpleDateFormat("MM月dd日", Locale.CHINA).format(target.time))
        }
    }
}
