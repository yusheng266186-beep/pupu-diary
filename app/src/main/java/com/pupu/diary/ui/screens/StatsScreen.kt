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
import com.pupu.diary.data.extraInfo
import com.pupu.diary.util.buildBars
import com.pupu.diary.util.buildInsights
import com.pupu.diary.util.formatFullCn
import com.pupu.diary.util.toLocalDate
import com.pupu.diary.util.toStartMs
import com.pupu.diary.util.weekdayCn
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    state: AppState,
    onEdit: (PoopRecord) -> Unit = {},
    onShowAll: () -> Unit = {}
) {
    val palette = LocalAppPalette.current
    var range by remember { mutableIntStateOf(1) }
    // v4.0.0：翻月偏移提升到本层，供年视图下钻联动
    var monthOffset by remember { mutableIntStateOf(0) }

    // v4.0.0：分钟级"今天"刷新，长驻统计页跨午夜后范围与日卡自动更新
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            val d = LocalDate.now()
            if (d != today) today = d
            delay(60_000)
        }
    }
    val todayStart = today.toStartMs()
    val startMs = when (range) {
        0 -> todayStart
        1 -> today.minusDays(6).toStartMs()
        3 -> today.minusMonths(11).withDayOfMonth(1).toStartMs()
        else -> today.minusDays(29).toStartMs()
    }
    val inRange = state.records.filter { it.time >= startMs }
    // 数据戳：条数 + 逐条哈希，编辑/增删记录（含颜色与状态字段）后缓存图表自动失效重算
    val dataStamp = state.records.size * 31L + state.records.sumOf { it.hashCode().toLong() }

    // 环比：与等长的上一周期对比（日 vs 昨天 / 周 vs 前 7 天 / 月 vs 前 30 天 / 年 vs 前 12 个月）
    val prevStartMs = when (range) {
        0 -> today.minusDays(1).toStartMs()
        1 -> today.minusDays(13).toStartMs()
        3 -> today.minusMonths(23).withDayOfMonth(1).toStartMs()
        else -> today.minusDays(59).toStartMs()
    }
    val prevRange = state.records.filter { it.time >= prevStartMs && it.time < startMs }
    val prevAvg = if (prevRange.isNotEmpty()) prevRange.map { it.smoothness }.average().roundToInt() else null
    // 图表与洞察构建已抽到 util/Insights.kt（纯 JVM，有单测）
    val (bars, labels) = remember(range, dataStamp, today) {
        buildBars(state.records, range, today)
    }

    val total = inRange.size
    val animTotal by animateIntAsState(targetValue = total, animationSpec = tween(600), label = "total")
    val avgSmooth = if (inRange.isNotEmpty()) inRange.map { it.smoothness }.average().roundToInt() else 0
    val daysCount = inRange.map { it.time.toLocalDate() }.distinct().size
    // v4.0.0：三指标全部滚动递增（此前只有次数有动画）
    val animAvg by animateIntAsState(targetValue = avgSmooth, animationSpec = tween(600), label = "avg")
    val animDays by animateIntAsState(targetValue = daysCount, animationSpec = tween(600), label = "days")
    val common = inRange.groupingBy { it.bristol }.eachCount().maxByOrNull { it.value }?.key
    val segments = (0..3).map { g -> g to inRange.count { Bristol.groupOf(it.bristol) == g } }
        .filter { it.second > 0 }
    val insightLines = remember(dataStamp, state.checkins.size) {
        buildInsights(state.records, state.checkins, today)
    }
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
                    SummaryMetric("$animDays", "记录天数")
                    SummaryMetric("$animAvg%", "平均顺滑")
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

        RangeCalendar(
            state.records, dataStamp, range, monthOffset,
            onMonthChange = { monthOffset = it },
            onPickMonth = { ym ->
                // v4.0.0：年视图格子可点，下钻到对应月的月视图
                range = 2
                monthOffset = java.time.temporal.ChronoUnit.MONTHS
                    .between(java.time.YearMonth.from(today), ym).toInt()
            },
            onEdit = onEdit,
            onDelete = { state.removeWithUndo(it) }
        )
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
        // v4.0.0：时间线入口——最近记录不再只给 8 条
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("最近记录", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = palette.textMain)
            Spacer(Modifier.weight(1f))
            Text(
                "查看全部 ›",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.primaryDeep,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onShowAll() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
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
                        val extra = r.extraInfo()
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
            // v4.0.0：删除钮触控区 36→44dp
            Box(
                Modifier
                    .size(44.dp)
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

/** 范围日历：随上方日/周/月/年切换视图形态（单日卡 / 周条 / 月格 / 年格），切换带缩放过渡；有记录的日子可点开当日明细 */
@Composable
private fun RangeCalendar(
    records: List<PoopRecord>,
    dataStamp: Long,
    range: Int,
    monthOffset: Int,
    onMonthChange: (Int) -> Unit,
    onPickMonth: (java.time.YearMonth) -> Unit,
    onEdit: (PoopRecord) -> Unit,
    onDelete: (PoopRecord) -> Unit
) {
    val palette = LocalAppPalette.current
    val today = LocalDate.now()
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    val counts = remember(dataStamp) {
        records.groupBy { it.time.toLocalDate() }.mapValues { it.value.size }
    }
    val monthCounts = remember(dataStamp) {
        records.groupingBy { java.time.YearMonth.from(it.time.toLocalDate()) }.eachCount()
    }
    // v4.0.0：日历格画当天最常见形状（此前恒画 4 型，图标不带信息量）
    val dayTypes = remember(dataStamp) {
        records.groupBy { it.time.toLocalDate() }.mapValues { e ->
            e.value.groupingBy { it.bristol }.eachCount().maxBy { it.value }.key
        }
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
                        firstType = dayTypes[today],
                        onClick = { selectedDay = today }
                    )
                    1 -> WeekStrip(today, counts, dayTypes) { selectedDay = it }
                    3 -> YearGrid(today, monthCounts, onPickMonth)
                    else -> MonthGrid(counts, dayTypes, monthOffset, { onMonthChange(it) }) { selectedDay = it }
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
                                        val extra = r.extraInfo()
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
    dayTypes: Map<LocalDate, Int>,
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
                        BristolIcon(type = dayTypes[date] ?: 4, size = 20.dp)
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
    dayTypes: Map<LocalDate, Int>,
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
                                            BristolIcon(type = dayTypes[date] ?: 4, size = 15.dp)
                                            // v4.0.0：8sp→10sp，小屏上更易读
                                            Text(
                                                "$day",
                                                fontSize = 10.sp,
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

/** 年视图：近 12 个月格子（v4.0.0：点击任意月格下钻到月视图） */
@Composable
private fun YearGrid(
    today: LocalDate,
    monthCounts: Map<java.time.YearMonth, Int>,
    onPickMonth: (java.time.YearMonth) -> Unit
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
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onPickMonth(ym) }
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
    // v4.0.0：视觉 30dp，触控热区扩到 44dp
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .semantics { contentDescription = label }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(palette.cardAlt),
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
}
