package com.pupu.diary.util

import com.pupu.diary.data.Moods
import com.pupu.diary.data.PoopRecord
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * 纯逻辑统计层（无 Android/Compose 依赖，可 JVM 单测）。
 * v4.0.0 从 StatsScreen 抽出 buildBars/buildInsights 并新增打卡关联洞察。
 */

/** 柱状数据：日=12 个两小时段 / 周=7 天 / 月=10 个三天段 / 年=12 个月 */
fun buildBars(
    records: List<PoopRecord>,
    range: Int,
    today: LocalDate
): Pair<List<Float>, List<String>> {
    val zone = ZoneId.systemDefault()
    val todayStart = today.toStartMs()
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
        else -> {
            val start = today.minusMonths(11).withDayOfMonth(1)
            val values = MutableList(12) { 0f }
            records.forEach { r ->
                val diff = ChronoUnit.MONTHS.between(start, YearMonth.from(r.time.toLocalDate()))
                if (diff in 0..11) values[diff.toInt()] += 1f
            }
            val labels = (0..11).map { m -> "${start.plusMonths(m.toLong()).monthValue}月" }
            values to labels
        }
    }
}

/** 连续有记录的天数（今天或昨天起算） */
fun streakDays(records: List<PoopRecord>, today: LocalDate): Int {
    if (records.isEmpty()) return 0
    val days = records.map { it.time.toLocalDate() }.toHashSet()
    var d = today
    if (d !in days) d = d.minusDays(1)
    var n = 0
    while (d in days) {
        n++
        d = d.minusDays(1)
    }
    return n
}

/**
 * 「规律小发现」洞察（全量历史，最近 10 条做身体状态预警）。
 * checkins: day(yyyyMMdd) -> [water, exercise, fiber]，用于喝水×顺滑度关联。
 */
fun buildInsights(
    records: List<PoopRecord>,
    checkins: Map<String, IntArray>,
    today: LocalDate
): List<String> {
    if (records.size < 3) return emptyList()
    val out = mutableListOf<String>()

    val streak = streakDays(records, today)
    if (streak >= 2) out.add("已经连续 $streak 天有记录啦，坚持得很棒！")

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

    val avg = records.map { it.smoothness }.average().roundToInt()
    out.add("平均顺滑度 $avg%，${if (avg >= 60) "状态在线" else "多喝水会更好"}")

    // 喝水×顺滑度关联：达标日 vs 有打卡未达标日，各 >=3 天才比较，差 >=5 分才提示
    if (checkins.isNotEmpty()) {
        val byDay = records.groupBy { it.time.toLocalDate() }
        val met = mutableListOf<Int>()
        val unmet = mutableListOf<Int>()
        byDay.forEach { (d, rs) ->
            val water = checkins[d.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)]?.get(0)
            if (water == null) return@forEach
            val avgDay = rs.map { it.smoothness }.average().roundToInt()
            if (water >= 8) met.add(avgDay) else unmet.add(avgDay)
        }
        if (met.size >= 3 && unmet.size >= 3) {
            val a = met.average().roundToInt()
            val b = unmet.average().roundToInt()
            if (a - b >= 5) {
                out.add("喝水达标的日子平均顺滑 $a%，比未达标高 ${a - b} 分，水分真的很管用")
            }
        }
    }

    // 身体状态预警（最近 10 条里出现较频才提示）
    val recent = records.take(10)
    val painCount = recent.count { it.abdominalPain }
    val redFlags = recent.count { it.colorType == 6 || it.colorType == 7 || it.colorType == 4 }
    if (painCount >= 3) {
        out.add("最近 10 条中有 $painCount 次腹痛，若持续不适建议咨询医生")
    } else if (recent.count { it.straining || it.incomplete } >= 3) {
        out.add("最近常觉得费力或没排净，多喝水、多吃纤维会舒服些")
    }
    if (redFlags >= 1) {
        out.add("最近出现过红棕/黑色/陶土色记录，这类颜色请多留意，持续出现建议就医")
    }

    val mood = records.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
    if (mood != null) out.add("记录时的心情多是「${Moods.labels[mood.coerceIn(0, 4)]}」")

    return out.take(5)
}
