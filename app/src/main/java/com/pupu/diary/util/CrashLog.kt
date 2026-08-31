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

    private const val MAX_CHARS = 60_000
    private const val FILE = "crash_log.txt"

    private fun logFile(context: Context) =
        File(File(context.filesDir, "logs").apply { mkdirs() }, FILE)

    /** 读取日志尾部（供应用内「诊断日志」查看），不存在时返回空串 */
    fun tail(context: Context, maxChars: Int = 4000): String {
        return try {
            val text = logFile(context).takeIf { it.exists() }?.readText() ?: ""
            if (text.length <= maxChars) text else "…（更早内容已省略）\n" + text.takeLast(maxChars)
        } catch (_: Exception) {
            ""
        }
    }

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val f = logFile(context)
                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
                val entry = buildString {
                    appendLine("==== $stamp (thread: ${t.name}) ====")
                    appendLine(Log.getStackTraceString(e))
                }
                synchronized(this) {
                    // 真正的环形保留：新内容在头部，超长时截尾部，而不是整份丢弃
                    val merged = entry + f.readText()
                    f.writeText(merged.take(MAX_CHARS))
                }
            } catch (_: Exception) {
            }
            previous?.uncaughtException(t, e)
        }
    }
}
