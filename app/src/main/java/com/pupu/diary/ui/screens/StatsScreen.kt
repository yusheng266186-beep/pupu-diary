package com.pupu.diary.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.data.Bristol
import com.pupu.diary.data.Moods
import com.pupu.diary.data.PoopColors
import com.pupu.diary.data.PoopRecord
import com.pupu.diary.ui.AppState
import com.pupu.diary.ui.components.BarChart
import com.pupu.diary.ui.components.BristolIcon
import com.pupu.diary.ui.components.DonutChart
import com.pupu.diary.ui.components.EmptyState
import com.pupu.diary.ui.components.PupuDialog
import com.pupu.diary.ui.components.SoftCard
import com.pupu.diary.ui.theme.LocalAppPalette
import com.pupu.diary.util.formatFullCn
import com.pupu.diary.util.toLocalDate
import com.pupu.diary.util.toStartMs
import com.pupu.diary.util.weekdayCn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun StatsScreen(state: AppState, onEdit: (PoopRecord) -> Unit = {}) {
    val palette = LocalAppPalette.current
    var range by remember { mutableIntStateOf(1) }

    val todayStart = remember { LocalDate.now().toStartMs() }
    val startMs = when (range) {
        0 -> todayStart
        1 -> LocalDate.now().minusDays(6).toStartMs()
        3 -> LocalDate.now().minusMonths(11).withDayOfMonth(1).toStartMs()
        else -> LocalDate.now().minusDays(29).toStartMs()
    }
    val inRange = state.records.filter { it.time >= startMs }
    // 数据戳：条数 + 逐条哈希，编辑/增删记录（含颜色与状态字段）后缓存图表自动失效重算
    val dataStamp = state.records.size * 31L + state.records.sumOf { it.hashCode().toLong() }

    // 环比：与等长的上一周期对比（日 vs 昨天 / 周 vs 前 7 天 / 月 vs 前 30 天 / 年 vs 前 12 个月）
    val prevStartMs = when (range) {
        0 -> LocalDate.now().minusDays(1).toStartMs()
        1 -> LocalDate.now().minusDays(13).toStartMs()
        3 -> LocalDate.now().minusMonths(23).withDayOfMonth(1).toStartMs()
        else -> LocalDate.now().minusDays(59).toStartMs()
    }
    val prevRange = state.records.filter { it.time >= prevStartMs && it.time < startMs }
    val prevAvg = if (prevRange.isNotEmpty()) prevRange.map { it.smoothness }.average().roundToInt() else null
    val (bars, labels) = remember(range, dataStamp) {
        buildBars(state.records, range, todayStart)
    }

    val total = inRange.size
    val animTotal by animateIntAsState(
        targetValue = total,
        animationSpec = tween(600),
        label = "total"
    )
    val avgSmooth = if (inRange.isNotEmpty()) inRange.map { it.smoothness }.average().roundToInt() else 0
    val daysCount = inRange.map { it.time.toLocalDate() }.distinct().size
    val common = inRange.groupingBy { it.bristol }.eachCount().maxByOrNull { it.value }?.key
    val segments = (0..3).map { g -> g to inRange.count { Bristol.groupOf(it.bristol) == g } }
        .filter { it.second > 0 }
    val insightLines = remember(dataStamp) { buildInsights(state.records) }
    val rangeTitle = listOf("今日", "最近 7 天", "最近 30 天", "近 12 个月")[range]

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text("统计分析", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = palette.textMain)
        Spacer(Modifier.height(14.dp))
        Segmented(listOf("日", "周", "月", "年"), range) { range = it }
        Spacer(Modifier.height(14.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rangeTitle, fontSize = 13.sp, color = palette.textSub)
                    Spacer(Modifier.weight(1f))
                    Chip("最常见 ${common?.toString() ?: "–"} 型")
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    SummaryMetric("$animTotal", "记录次数")
                    SummaryMetric("$daysCount", "记录天数")
                    SummaryMetric("$avgSmooth%", "平均顺滑")
                }
                // 环比行入场：淡入 + 上移（仅在上一周期有数据时出现）
                androidx.compose.animation.AnimatedVisibility(
                    visible = prevRange.isNotEmpty(),
                    enter = androidx.compose.animation.fadeIn(tween(220)) +
                        androidx.compose.animation.slideInVertically(tween(220)) { it / 3 }
                ) {
                    Column {
                        Spacer(Modifier.height(10.dp))
                        DeltaLine(
                            dTotal = total - prevRange.size,
                            dDays = daysCount - prevRange.map { it.time.toLocalDate() }.distinct().size,
                            dSmooth = prevAvg?.let { avgSmooth - it }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        if (insightLines.isNotEmpty()) {
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "规律小发现",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textMain
                    )
                    Spacer(Modifier.height(8.dp))
                    insightLines.forEach { line ->
                        Row(
                            Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(line, fontSize = 12.sp, color = palette.textSub)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        RangeCalendar(state.records, dataStamp, range, onEdit = onEdit, onDelete = { state.removeWithUndo(it) })
        Spacer(Modifier.height(14.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("记录趋势", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
                Spacer(Modifier.height(10.dp))
                BarChart(values = bars, labels = labels)
            }
        }
        Spacer(Modifier.height(14.dp))

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("形状分布", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
                Spacer(Modifier.height(10.dp))
                if (segments.isEmpty()) {
                    EmptyState(
                        title = "这里还空空的～",
                        subtitle = "记录几次后，这里会画出你的形状分布。"
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            DonutChart(
                                segments = segments.map {
                                    Bristol.groupColors[it.first] to it.second.toFloat()
                                }
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$total", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = palette.textMain)
                                Text("条记录", fontSize = 11.sp, color = palette.textSub)
                            }
                        }
                        Spacer(Modifier.width(20.dp))
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            segments.sortedByDescending { it.second }.forEach { (g, c) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Bristol.groupColors[g])
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        Bristol.groupNames[g],
                                        fontSize = 12.sp,
                                        color = palette.textMain,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${(c * 100f / total).roundToInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textSub
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("最近记录", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
        Spacer(Modifier.height(8.dp))
        if (state.records.isEmpty()) {
            EmptyState(
                title = "还没有记录",
                subtitle = "去记第一条吧，噗噗在等你～",
                characterSize = 52.dp
            )
        }
        state.records.take(8).forEach { r ->
            RecentRow(r, onEdit = onEdit, onDelete = { state.removeWithUndo(r) })
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Chip(text: String) {
    val palette = LocalAppPalette.current
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(50),
        color = palette.primarySoft
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = palette.primaryDeep
        )
    }
}

/** 三指标总览的单个指标 */
@Composable
private fun SummaryMetric(value: String, label: String) {
    val palette = LocalAppPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = palette.primaryDeep
        )
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = palette.textSub)
    }
}

/** 环比摘要：与上一周期对比，正向主色深绿、负向暖红 */
@Composable
private fun DeltaLine(dTotal: Int, dDays: Int, dSmooth: Int?) {
    val palette = LocalAppPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("较上一周期", fontSize = 11.sp, color = palette.textSub)
        Spacer(Modifier.width(12.dp))
        DeltaText("次数", dTotal)
        Spacer(Modifier.width(12.dp))
        DeltaText("天数", dDays)
        dSmooth?.let {
            Spacer(Modifier.width(12.dp))
            DeltaText("顺滑", it)
        }
    }
}

@Composable
private fun DeltaText(label: String, delta: Int) {
    val palette = LocalAppPalette.current
    val sign = if (delta > 0) "+" else ""
    Text(
        "$label $sign$delta",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (delta >= 0) palette.primaryDeep else Color(0xFFD06A5A)
    )
}

/** 颜色 + 身体状态的附加摘要（仅在记录里出现时展示） */
private fun extraInfo(r: PoopRecord): String {
    val parts = mutableListOf<String>()
    r.colorType?.let { parts.add(PoopColors.label(it)) }
    if (r.abdominalPain) parts.add("腹痛")
    if (r.straining) parts.add("费力")
    if (r.incomplete) parts.add("未排净")
    return parts.joinToString(" · ")
}

@Composable
private fun Segmented(options: List<String>, selected: Int, onChange: (Int) -> Unit) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .background(palette.cardAlt)
            .padding(4.dp)
    ) {
        val itemW = maxWidth / options.size
        val thumbX by animateDpAsState(
            targetValue = itemW * selected,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
            label = "segX"
        )
        Box(
            Modifier
                .offset(x = thumbX)
                .width(itemW)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(palette.primarySoft)
        )
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, o ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onChange(i)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val color by animateColorAsState(
                        targetValue = if (i == selected) palette.primaryDeep else palette.textSub,
                        animationSpec = tween(250),
                        label = "segC"
                    )
                    Text(
                        o,
                        fontSize = 14.sp,
                        fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentRow(r: PoopRecord, onEdit: (PoopRecord) -> Unit, onDelete: () -> Unit) {
    val palette = LocalAppPalette.current
    SoftCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onEdit(r) }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                BristolIcon(type = r.bristol, size = 26.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    formatFullCn(r.time),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textMain
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append("${r.bristol}型 ${Bristol.label(r.bristol)} · 心情${Moods.labels[r.mood.coerceIn(0, 4)]}")
                        if (r.durationMin > 0) append(" · ${r.durationMin} 分钟")
                        val extra = extraInfo(r)
                        if (extra.isNotEmpty()) append(" · $extra")
                    },
                    fontSize = 11.sp,
                    color = palette.textSub
                )
                if (r.note.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "「${r.note}」",
                        fontSize = 11.sp,
                        color = palette.primaryDeep,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(50),
                color = palette.primarySoft
            ) {
                Text(
                    "顺滑 ${r.smoothness}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    color = palette.primaryDeep
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .size(36.dp)
                    .semantics { contentDescription = "删除这条记录" }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(Modifier.size(12.dp)) {
                    drawLine(
                        palette.textSub,
                        Offset(0f, 0f), Offset(size.width, size.height),
                        strokeWidth = size.width * 0.16f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    drawLine(
                        palette.textSub,
                        Offset(size.width, 0f), Offset(0f, size.height),
                        strokeWidth = size.width * 0.16f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

private fun buildBars(
    records: List<PoopRecord>,
    range: Int,
    todayStart: Long
): Pair<List<Float>, List<String>> {
    val zone = ZoneId.systemDefault()
    return when (range) {
        0 -> {
            val values = MutableList(12) { 0f }
            records.forEach { r ->
                if (r.time >= todayStart) {
                    val hour = Instant.ofEpochMilli(r.time).atZone(zone).hour
                    values[hour / 2] += 1f
                }
            }
            values to (0..11).map { "${it * 2}" }
        }
        1 -> {
            val today = LocalDate.now()
            val values = MutableList(7) { 0f }
            (0..6).forEach { d ->
                val date = today.minusDays((6 - d).toLong())
                values[d] = records.count {
                    it.time >= date.toStartMs() && it.time < date.plusDays(1).toStartMs()
                }.toFloat()
            }
            values to (0..6).map { d ->
                weekdayCn(today.minusDays((6 - d).toLong())).substring(1)
            }
        }
        2 -> {
            val today = LocalDate.now()
            val values = MutableList(10) { 0f }
            (0..9).forEach { b ->
                val s = today.minusDays((29 - b * 3).toLong()).toStartMs()
                val e = today.minusDays((26 - b * 3).toLong()).toStartMs()
                values[b] = records.count { it.time >= s && it.time < e }.toFloat()
            }
            values to (0..9).map { b ->
                val start = today.minusDays((29 - b * 3).toLong())
                "${start.monthValue}/${start.dayOfMonth}"
            }
        }
        else -> buildMonthBars(records)
    }
}

/** 近 12 个月柱状数据 */
private fun buildMonthBars(records: List<PoopRecord>): Pair<List<Float>, List<String>> {
    val start = LocalDate.now().minusMonths(11).withDayOfMonth(1)
    val values = MutableList(12) { 0f }
    records.forEach { r ->
        val d = r.time.toLocalDate()
        val diff = (d.year - start.year) * 12 + (d.monthValue - start.monthValue)
        if (diff in 0..11) values[diff] += 1f
    }
    val labels = (0..11).map { b -> "${start.plusMonths(b.toLong()).monthValue}月" }
    return values to labels
}

/** 连续有记录的天数（今天或昨天起算） */
private fun streakDays(records: List<PoopRecord>): Int {
    if (records.isEmpty()) return 0
    val days = records.map { it.time.toLocalDate() }.toHashSet()
    var d = LocalDate.now()
    if (d !in days) d = d.minusDays(1)
    var n = 0
    while (d in days) {
        n++
        d = d.minusDays(1)
    }
    return n
}

private fun buildInsights(records: List<PoopRecord>): List<String> {
    if (records.size < 3) return emptyList()
    val out = mutableListOf<String>()

    // 连续记录天数
    val streak = streakDays(records)
    if (streak >= 2) out.add("已经连续 $streak 天有记录啦，坚持得很棒！")

    // 常打卡时段
    val zone = ZoneId.systemDefault()
    val bucket = records.groupBy { r ->
        val h = Instant.ofEpochMilli(r.time).atZone(zone).hour
        when (h) {
            in 5..10 -> "清晨"
            in 11..13 -> "中午"
            in 14..17 -> "下午"
            in 18..22 -> "晚上"
            else -> "深夜"
        }
    }.maxByOrNull { it.value.size }?.key
    if (bucket != null) out.add("你最常在${bucket}打卡～")

    // 平均顺滑度
    val avg = records.map { it.smoothness }.average().roundToInt()
    out.add("平均顺滑度 $avg%，${if (avg >= 60) "状态在线" else "多喝水会更好"}")

    // 身体状态预警（v2.2.0 新增字段，最近 10 条里出现较频才提示）
    val recent = records.take(10)
    val painCount = recent.count { it.abdominalPain }
    if (painCount >= 3) {
        out.add("最近 10 条中有 $painCount 次腹痛，若持续不适建议咨询医生")
    } else if (recent.count { it.straining || it.incomplete } >= 3) {
        out.add("最近常觉得费力或没排净，多喝水、多吃纤维会舒服些")
    }

    // 常见心情
    val mood = records.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
    if (mood != null) out.add("记录时的心情多是「${Moods.labels[mood.coerceIn(0, 4)]}」")

    return out.take(4)
}

/** 范围日历：随上方日/周/月/年切换视图形态（单日卡 / 周条 / 月格 / 年格），切换带缩放过渡；有记录的日子可点开当日明细 */
@Composable
private fun RangeCalendar(
    records: List<PoopRecord>,
    dataStamp: Long,
    range: Int,
    onEdit: (PoopRecord) -> Unit,
    onDelete: (PoopRecord) -> Unit
) {
    val palette = LocalAppPalette.current
    val today = LocalDate.now()
    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    val counts = remember(dataStamp) {
        records.groupBy { it.time.toLocalDate() }.mapValues { it.value.size }
    }
    val monthCounts = remember(dataStamp) {
        records.groupingBy { java.time.YearMonth.from(it.time.toLocalDate()) }.eachCount()
    }

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            AnimatedContent(
                targetState = range,
                transitionSpec = {
                    (fadeIn(tween(240)) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f)
                    )) togetherWith fadeOut(tween(150))
                },
                label = "rangeCal"
            ) { r ->
                when (r) {
                    0 -> DayCard(
                        date = today,
                        count = counts[today] ?: 0,
                        firstType = records.firstOrNull { it.time.toLocalDate() == today }?.bristol,
                        onClick = { selectedDay = today }
                    )
                    1 -> WeekStrip(today, counts) { selectedDay = it }
                    3 -> YearGrid(today, monthCounts)
                    else -> MonthGrid(counts, monthOffset, { monthOffset = it }) { selectedDay = it }
                }
            }
        }
    }

    // 当日明细弹层：点某天查看该天全部记录，可编辑 / 删除
    selectedDay?.let { day ->
        val dayRecords = records
            .filter { it.time.toLocalDate() == day }
            .sortedByDescending { it.time }
        PupuDialog(
            onDismissRequest = { selectedDay = null },
            title = "${day.monthValue} 月 ${day.dayOfMonth} 日 · ${dayRecords.size} 条记录",
            content = {
                Column {
                    if (dayRecords.isEmpty()) {
                        Text("这天没有记录～", fontSize = 13.sp, color = palette.textSub)
                    }
                    dayRecords.forEach { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedDay = null
                                    onEdit(r)
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(palette.primarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                BristolIcon(type = r.bristol, size = 24.dp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    formatFullCn(r.time),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textMain
                                )
                                Text(
                                    buildString {
                                        append("${r.bristol}型 ${Bristol.label(r.bristol)} · 顺滑 ${r.smoothness}%")
                                        if (r.durationMin > 0) append(" · ${r.durationMin} 分钟")
                                        val extra = extraInfo(r)
                                        if (extra.isNotEmpty()) append(" · $extra")
                                    },
                                    fontSize = 11.sp,
                                    color = palette.textSub
                                )
                            }
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .semantics { contentDescription = "删除这条记录" }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedDay = null
                                        onDelete(r)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(Modifier.size(11.dp)) {
                                    drawLine(
                                        palette.textSub,
                                        Offset(0f, 0f), Offset(size.width, size.height),
                                        strokeWidth = size.width * 0.16f,
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        palette.textSub,
                                        Offset(size.width, 0f), Offset(0f, size.height),
                                        strokeWidth = size.width * 0.16f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                    if (dayRecords.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "点一条可编辑 · 右侧 × 删除",
                            fontSize = 10.sp,
                            color = palette.textSub.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            confirmText = "关闭"
        )
    }
}

/** 日视图：今天一张大卡片 */
@Composable
private fun DayCard(date: LocalDate, count: Int, firstType: Int?, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.primarySoft.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(palette.card),
            contentAlignment = Alignment.Center
        ) {
            BristolIcon(type = firstType ?: 4, size = 34.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${date.monthValue} 月 ${date.dayOfMonth} 日",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textMain
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${weekdayCn(date)} · 记录 $count 次",
                fontSize = 12.sp,
                color = palette.textSub
            )
        }
        Text(
            if (count > 0) "看明细" else "去记录",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.primaryDeep
        )
        Spacer(Modifier.width(6.dp))
        Canvas(Modifier.size(8.dp, 14.dp)) {
            val p = Path().apply {
                moveTo(size.width * 0.25f, size.height * 0.16f)
                lineTo(size.width * 0.78f, size.height * 0.5f)
                lineTo(size.width * 0.25f, size.height * 0.84f)
            }
            drawPath(p, palette.primaryDeep, style = Stroke(size.width * 0.3f, cap = StrokeCap.Round))
        }
    }
}

/** 周视图：最近 7 天一条 */
@Composable
private fun WeekStrip(
    today: LocalDate,
    counts: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit
) {
    val palette = LocalAppPalette.current
    Row(Modifier.fillMaxWidth()) {
        (0..6).forEach { i ->
            val date = today.minusDays((6 - i).toLong())
            val count = counts[date] ?: 0
            val isToday = date == today
            Column(
                Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = count > 0
                    ) { onDayClick(date) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    weekdayCn(date).substring(1),
                    fontSize = 10.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) palette.primaryDeep else palette.textSub
                )
                Spacer(Modifier.height(5.dp))
                val ring = if (isToday) Modifier.border(1.5.dp, palette.primary, CircleShape) else Modifier
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (count > 0) palette.primarySoft else palette.cardAlt.copy(alpha = 0.35f))
                        .then(ring),
                    contentAlignment = Alignment.Center
                ) {
                    if (count > 0) {
                        BristolIcon(type = 4, size = 20.dp)
                    } else {
                        Text("${date.dayOfMonth}", fontSize = 11.sp, color = palette.textSub)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${date.dayOfMonth}",
                    fontSize = 9.sp,
                    fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (count > 0) palette.primaryDeep else palette.textSub
                )
            }
        }
    }
}

/** 月视图：可翻月的整月格子 */
@Composable
private fun MonthGrid(
    counts: Map<LocalDate, Int>,
    monthOffset: Int,
    onMonthChange: (Int) -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val palette = LocalAppPalette.current
    val today = LocalDate.now()
    val base = remember(monthOffset) { LocalDate.now().plusMonths(monthOffset.toLong()) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${base.year} 年 ${base.monthValue} 月",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textMain
            )
            Spacer(Modifier.weight(1f))
            NavCircle(-1, "上个月") { onMonthChange(monthOffset - 1) }
            Spacer(Modifier.width(8.dp))
            NavCircle(1, "下个月") { onMonthChange(monthOffset + 1) }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { w ->
                Text(
                    w,
                    fontSize = 11.sp,
                    color = palette.textSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        AnimatedContent(
            targetState = base,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(280)) { dir * it / 4 } + fadeIn(tween(220))) togetherWith
                    (slideOutHorizontally(tween(280)) { -dir * it / 4 } + fadeOut(tween(160)))
            },
            label = "monthGrid"
        ) { m ->
            val firstDay = m.withDayOfMonth(1)
            val daysInMonth = firstDay.lengthOfMonth()
            val lead = firstDay.dayOfWeek.value - 1 // 周一开头
            val padded = ((lead + daysInMonth + 6) / 7) * 7
            Column {
                (0 until padded).chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { idx ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val day = idx - lead + 1
                                if (day in 1..daysInMonth) {
                                    val date = m.withDayOfMonth(day)
                                    val count = counts[date] ?: 0
                                    val isToday = date == today
                                    val ring = if (isToday) Modifier.border(1.5.dp, palette.primary, CircleShape) else Modifier
                                    if (count > 0) {
                                        Column(
                                            Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(palette.primarySoft)
                                                .then(ring)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) { onDayClick(date) },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            BristolIcon(type = 4, size = 15.dp)
                                            Text(
                                                "$day",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.primaryDeep
                                            )
                                        }
                                    } else {
                                        Box(
                                            Modifier
                                                .size(34.dp)
                                                .then(ring),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$day", fontSize = 11.sp, color = palette.textSub)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 年视图：近 12 个月格子 */
@Composable
private fun YearGrid(
    today: LocalDate,
    monthCounts: Map<java.time.YearMonth, Int>
) {
    val palette = LocalAppPalette.current
    val start = today.minusMonths(11).withDayOfMonth(1)
    val current = java.time.YearMonth.from(today)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (0..2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (0..3).forEach { col ->
                    val m = start.plusMonths((row * 4 + col).toLong())
                    val ym = java.time.YearMonth.from(m)
                    val count = monthCounts[ym] ?: 0
                    val isCurrent = ym == current
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (count > 0) palette.primarySoft else palette.cardAlt.copy(alpha = 0.4f))
                            .then(
                                if (isCurrent) Modifier.border(1.5.dp, palette.primary, RoundedCornerShape(14.dp)) else Modifier
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${m.monthValue}月",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (count > 0 || isCurrent) palette.primaryDeep else palette.textSub
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (count > 0) "$count 条" else "—",
                                fontSize = 10.sp,
                                color = if (count > 0) palette.primaryDeep else palette.textSub
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavCircle(direction: Int, label: String, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(palette.cardAlt)
            .semantics { contentDescription = label }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(12.dp)) {
            val p = Path()
            if (direction < 0) {
                p.moveTo(size.width * 0.65f, size.height * 0.2f)
                p.lineTo(size.width * 0.35f, size.height * 0.5f)
                p.lineTo(size.width * 0.65f, size.height * 0.8f)
            } else {
                p.moveTo(size.width * 0.35f, size.height * 0.2f)
                p.lineTo(size.width * 0.65f, size.height * 0.5f)
                p.lineTo(size.width * 0.35f, size.height * 0.8f)
            }
            drawPath(
                p, palette.textMain,
                style = Stroke(width = size.width * 0.22f, cap = StrokeCap.Round)
            )
        }
    }
}
