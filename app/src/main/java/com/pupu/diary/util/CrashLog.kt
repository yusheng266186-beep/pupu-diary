package com.pupu.diary.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 本地崩溃记录：未捕获异常先追加写入 files/logs/crash_log.txt（超过 ~60KB 丢弃旧内容），
 * 再交回系统默认处理器，不影响原有崩溃流程。便于用户反馈问题时导出排查。
 */
object CrashLog {

    private const val MAX_BYTES = 60_000

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val dir = File(context.filesDir, "logs").apply { mkdirs() }
                val f = File(dir, "crash_log.txt")
                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
                val entry = buildString {
                    appendLine("==== $stamp (thread: ${t.name}) ====")
                    appendLine(Log.getStackTraceString(e))
                }
                synchronized(this) {
                    val old = if (f.length() in 1..MAX_BYTES) f.readText() else ""
                    f.writeText(entry + old)
                }
            } catch (_: Exception) {
            }
            previous?.uncaughtException(t, e)
        }
    }
}
