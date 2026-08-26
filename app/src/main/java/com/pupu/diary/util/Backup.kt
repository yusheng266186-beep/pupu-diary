package com.pupu.diary.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.pupu.diary.data.PoopRecord
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object Backup {

    fun toJsonBytes(records: List<PoopRecord>): ByteArray {
        val arr = JSONArray()
        records.forEach { r ->
            arr.put(
                JSONObject()
                    .put("time", r.time)
                    .put("bristol", r.bristol)
                    .put("mood", r.mood)
                    .put("smoothness", r.smoothness)
                    .put("note", r.note)
                    .put("duration", r.durationMin)
            )
        }
        val json = JSONObject()
            .put("app", "pupu-diary")
            .put("version", 3)
            .put("records", arr)
        return json.toString(2).toByteArray(Charsets.UTF_8)
    }

/** 导出结果：展示路径 + 可分享的 content Uri */
class ExportResult(val displayPath: String, val uri: Uri)

/** 导出全部记录为 JSON，返回可展示的路径与可分享的 Uri */
fun export(context: Context, records: List<PoopRecord>): ExportResult {
    val bytes = toJsonBytes(records)
    val name = "pupu_backup_${todayKey()}.json"

    if (Build.VERSION.SDK_INT >= 29) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri: Uri? = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
        )
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            return ExportResult("下载/$name", uri)
        }
    }
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        ?: context.filesDir
    val f = File(dir, name)
    f.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", f)
    return ExportResult(f.absolutePath, uri)
}

    /** 静默自动备份到应用专属目录（不占用户空间视野），保留最近 KEEP 份 */
    fun autoBackup(context: Context, records: List<PoopRecord>) {
        if (records.isEmpty()) return
        val dir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        dir.listFiles()?.sortedByDescending { it.name }?.drop(KEEP - 1)?.forEach { it.delete() }
        File(dir, "pupu_auto_${todayKey()}.json").writeBytes(toJsonBytes(records))
    }

    /** 解析备份 JSON，返回合法记录（按时间升序） */
    fun parse(json: String): List<PoopRecord> {
        return try {
            val arr = JSONObject(json).optJSONArray("records") ?: JSONArray()
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val bristol = o.optInt("bristol", 0)
                val time = o.optLong("time", 0L)
                if (bristol in 1..7 && time > 0) {
                    PoopRecord(
                        time = time,
                        bristol = bristol,
                        mood = o.optInt("mood", 1).coerceIn(0, 4),
                        smoothness = o.optInt("smoothness", 50).coerceIn(0, 100),
                        note = o.optString("note", ""),
                        durationMin = o.optInt("duration", 0).coerceIn(0, 120)
                    )
                } else {
                    null
                }
            }.sortedBy { it.time }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private const val KEEP = 4
}
