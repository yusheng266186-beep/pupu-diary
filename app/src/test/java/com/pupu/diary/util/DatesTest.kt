package com.pupu.diary.util

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.TimeZone

class DatesTest {

    @Before
    fun setUp() {
        // 固定时区，保证在不同机器 / CI 上结果一致
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun epochMilliToLocalDateRoundTrip() {
        val date = LocalDate.of(2026, 8, 30)
        assertEquals(date, date.toStartMs().toLocalDate())
        // 纪元 0 对应 1970-01-01
        assertEquals(LocalDate.of(1970, 1, 1), 0L.toLocalDate())
    }

    @Test
    fun formatDateCnOutputsChineseMonthDay() {
        assertEquals("8月30日", formatDateCn(LocalDate.of(2026, 8, 30).toStartMs()))
        assertEquals("1月1日", formatDateCn(LocalDate.of(1970, 1, 1).toStartMs()))
        assertEquals("12月31日", formatDateCn(LocalDate.of(1999, 12, 31).toStartMs()))
    }

    @Test
    fun formatTimeCnPadsHourAndMinute() {
        assertEquals("08:05", formatTimeCn(Instant.parse("2026-08-30T08:05:00Z").toEpochMilli()))
        assertEquals("00:00", formatTimeCn(0L))
        assertEquals("23:59", formatTimeCn(Instant.parse("2026-08-30T23:59:00Z").toEpochMilli()))
    }

    @Test
    fun formatFullCnCombinesDateAndTime() {
        val ms = Instant.parse("2026-08-30T08:30:00Z").toEpochMilli()
        assertEquals("8月30日 08:30", formatFullCn(ms))
    }

    @Test
    fun weekdayCnMapsAllSevenDays() {
        // 2026-08-24 是周一
        val monday = LocalDate.of(2026, 8, 24)
        val expected = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        expected.forEachIndexed { i, name ->
            assertEquals(name, weekdayCn(monday.plusDays(i.toLong())))
        }
    }

    @Test
    fun minutesToCnFormatsDuration() {
        assertEquals("00:00", minutesToCn(0))
        assertEquals("01:05", minutesToCn(65))
        assertEquals("10:00", minutesToCn(600))
        assertEquals("23:55", minutesToCn(1435))
    }
}
