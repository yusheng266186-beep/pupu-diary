package com.pupu.diary.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val zone: ZoneId = ZoneId.systemDefault()

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun LocalDate.toStartMs(): Long =
    this.atStartOfDay(zone).toInstant().toEpochMilli()

fun todayKey(): String = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)

fun formatDateCn(ms: Long): String {
    val d = ms.toLocalDate()
    return "${d.monthValue}月${d.dayOfMonth}日"
}

fun formatTimeCn(ms: Long): String {
    val t = Instant.ofEpochMilli(ms).atZone(zone).toLocalTime()
    return "%02d:%02d".format(t.hour, t.minute)
}

fun formatFullCn(ms: Long): String = "${formatDateCn(ms)} ${formatTimeCn(ms)}"

fun weekdayCn(d: LocalDate): String = when (d.dayOfWeek.value) {
    1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
    5 -> "周五"; 6 -> "周六"; else -> "周日"
}

fun minutesToCn(min: Int): String = "%02d:%02d".format(min / 60, min % 60)
