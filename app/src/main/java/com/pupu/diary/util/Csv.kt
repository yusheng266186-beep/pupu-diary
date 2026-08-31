package com.pupu.diary.util

import com.pupu.diary.data.Bristol
import com.pupu.diary.data.Moods
import com.pupu.diary.data.PoopColors
import com.pupu.diary.data.PoopRecord
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 医生查看版 CSV 导出：一行一次记录，含全部临床字段（时间/形状/颜色/心情/顺滑度/时长/状态/备注） */
object Csv {

    fun build(records: List<PoopRecord>): ByteArray {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        val out = StringBuilder()
        out.append("时间,便形状(Bristol),颜色,心情,顺滑度(%),时长(分钟),腹痛,费力,未排净,备注\r\n")
        records.sortedByDescending { it.time }.forEach { r ->
            out.append(escape(sdf.format(Date(r.time)))).append(',')
            out.append(escape("${r.bristol}(${Bristol.label(r.bristol)})")).append(',')
            out.append(escape(r.colorType?.let { PoopColors.label(it) } ?: "未记录")).append(',')
            out.append(escape(Moods.labels[r.mood.coerceIn(0, 4)])).append(',')
            out.append(r.smoothness).append(',')
            out.append(if (r.durationMin > 0) r.durationMin.toString() else "").append(',')
            out.append(if (r.abdominalPain) "是" else "").append(',')
            out.append(if (r.straining) "是" else "").append(',')
            out.append(if (r.incomplete) "是" else "").append(',')
            out.append(escape(r.note))
            out.append("\r\n")
        }
        // UTF-8 BOM：Windows Excel 直接双击打开不乱码
        val bos = ByteArrayOutputStream()
        bos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
        bos.write(out.toString().toByteArray(Charsets.UTF_8))
        return bos.toByteArray()
    }

    private fun escape(s: String): String =
        if (s.contains(',') || s.contains('"') || s.contains('\n')) {
            "\"" + s.replace("\"", "\"\"") + "\""
        } else {
            s
        }
}
