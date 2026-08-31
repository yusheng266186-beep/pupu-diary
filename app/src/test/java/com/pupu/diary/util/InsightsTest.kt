package com.pupu.diary.util

import com.pupu.diary.data.PoopRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.TimeZone

/** v4.0.0 新增：从 StatsScreen 抽出的纯逻辑统计层回归测试 */
class InsightsTest {

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private fun rec(day: LocalDate, hour: Int = 9, smooth: Int = 70, bristol: Int = 4, color: Int? = null) =
        PoopRecord(
            time = day.atStartOfDay().plusHours(hour.toLong())
                .atZone(TimeZone.getTimeZone("UTC").toZoneId())
                .toInstant().toEpochMilli(),
            bristol = bristol, mood = 1, smoothness = smooth, colorType = color
        )

    private val monday = LocalDate.of(2026, 8, 24)

    @Test
    fun barsForWeekRangeHasSevenColumns() {
        val records = listOf(rec(monday, smooth = 70), rec(monday.plusDays(3), smooth = 80))
        val (bars, labels) = buildBars(records, 1, monday.plusDays(6))
        assertEquals(7, bars.size)
        assertEquals(7, labels.size)
        // 周一有记录 → 第一列计数 1；周六（第 6 列）为 0
        assertEquals(1f, bars[0], 0.001f)
        assertEquals(1f, bars[3], 0.001f)
        assertEquals(0f, bars[5], 0.001f)
    }

    @Test
    fun insightsNeedAtLeastThreeRecords() {
        assertEquals(emptyList<String>(), buildInsights(listOf(rec(monday)), emptyMap(), monday))
    }

    @Test
    fun redFlagColorTriggersDoctorHint() {
        val lines = buildInsights(
            listOf(rec(monday), rec(monday.minusDays(1), color = 6), rec(monday.minusDays(2))),
            emptyMap(), monday
        )
        assertTrue(lines.any { it.contains("就医") })
    }

    @Test
    fun waterCorrelationAppearsWhenGapLarge() {
        // 三天喝水达标(>=8杯)且顺滑 85；三天未达标且顺滑 50 → 差 35 分，应出关联洞察
        val recs = (0..2).map { rec(monday.minusDays(it.toLong()), smooth = 85) } +
            (3..5).map { rec(monday.minusDays(it.toLong()), smooth = 50) }
        val checkins = HashMap<String, IntArray>()
        (0..2).forEach { i ->
            checkins[monday.minusDays(i.toLong()).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)] =
                intArrayOf(8, 0, 0)
        }
        (3..5).forEach { i ->
            checkins[monday.minusDays(i.toLong()).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)] =
                intArrayOf(2, 0, 0)
        }
        val lines = buildInsights(recs, checkins, monday)
        assertTrue(lines.any { it.contains("喝水达标") })
    }

    @Test
    fun streakCountsConsecutiveDaysIncludingToday() {
        val records = listOf(rec(monday), rec(monday.minusDays(1)), rec(monday.minusDays(2)))
        assertEquals(3, streakDays(records, monday))
    }
}
