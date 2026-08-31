package com.pupu.diary.util

import com.pupu.diary.data.PoopRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

/** v4.0.0 新增：医生查看版 CSV 的列序 / 转义 / 元信息 / BOM */
class CsvTest {

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private fun ms(y: Int, mo: Int, d: Int, h: Int, mi: Int): Long =
        LocalDateTime.of(y, mo, d, h, mi).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli()

    private fun sample(): List<PoopRecord> = listOf(
        PoopRecord(
            id = 1, time = ms(2026, 8, 30, 8, 5), bristol = 4, mood = 0, smoothness = 80,
            note = "吃了" + "辣条,还有\"引号\"", durationMin = 5, colorType = 6,
            abdominalPain = true, straining = false, incomplete = true
        ),
        PoopRecord(id = 2, time = ms(2026, 8, 29, 20, 0), bristol = 1, mood = 3, smoothness = 30)
    )

    @Test
    fun startsWithBomAndHeader() {
        val text = String(Csv.build(sample()), Charsets.UTF_8)
        assertTrue(text.startsWith("﻿"))
        assertTrue(text.contains("时间,便形状(Bristol),颜色,心情,顺滑度(%),时长(分钟),腹痛,费力,未排净,备注"))
    }

    @Test
    fun metaLinesPrefixedAndEscaped() {
        val text = String(Csv.build(sample(), listOf("导出日期：20260831", "条数,2")), Charsets.UTF_8)
        // 无逗号的元信息原样输出（带 # 前缀）；含逗号的被引号包裹
        assertTrue(text.contains("#导出日期：20260831"))
        assertTrue(text.contains("\"#条数,2\""))
    }

    @Test
    fun commasAndQuotesInNoteAreEscaped() {
        val text = String(Csv.build(sample()), Charsets.UTF_8)
        assertTrue(text.contains("\"吃了辣条,还有\"\"引号\"\"\""))
    }

    @Test
    fun clinicalFieldsRender() {
        val text = String(Csv.build(sample()), Charsets.UTF_8)
        // 颜色 6 = 黑色；布尔字段渲染为"是"，未出现留空；顺滑度为纯数字；无颜色记录渲染为"未记录"
        assertTrue(text.contains("4(泥块状),黑色,开心,80,5,是,,是"))
        assertTrue(text.contains("1(硬块状),未记录,难受,30,,,,"))
    }

    @Test
    fun sortedDescendingByTime() {
        val text = String(Csv.build(sample()), Charsets.UTF_8)
        assertTrue(text.indexOf("2026-08-30 08:05") in 0 until text.indexOf("2026-08-29 20:00"))
    }
}
