package com.pupu.diary.data

import android.content.Context
import android.content.SharedPreferences
import com.pupu.diary.util.todayKey

class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("pupu_prefs", Context.MODE_PRIVATE)

    var themeIndex: Int
        get() = sp.getInt("theme_index", 0)
        set(v) = sp.edit().putInt("theme_index", v).apply()

    var privacyMode: Boolean
        get() = sp.getBoolean("privacy_mode", false)
        set(v) = sp.edit().putBoolean("privacy_mode", v).apply()

    var recordRemindOn: Boolean
        get() = sp.getBoolean("record_remind_on", false)
        set(v) = sp.edit().putBoolean("record_remind_on", v).apply()

    var recordRemindMin: Int
        get() = sp.getInt("record_remind_min", 20 * 60 + 30)
        set(v) = sp.edit().putInt("record_remind_min", v).apply()

    var waterRemindOn: Boolean
        get() = sp.getBoolean("water_remind_on", false)
        set(v) = sp.edit().putBoolean("water_remind_on", v).apply()

    var waterRemindMin: Int
        get() = sp.getInt("water_remind_min", 9 * 60)
        set(v) = sp.edit().putInt("water_remind_min", v).apply()

    var autoBackupOn: Boolean
        get() = sp.getBoolean("auto_backup_on", true)
        set(v) = sp.edit().putBoolean("auto_backup_on", v).apply()

    var lastAutoBackup: String
        get() = sp.getString("last_auto_backup", "") ?: ""
        set(v) = sp.edit().putString("last_auto_backup", v).apply()

    var userName: String
        get() = sp.getString("user_name", "") ?: ""
        set(v) = sp.edit().putString("user_name", v).apply()

    /** 夜间模式：0 跟随系统 / 1 常开 / 2 常关 */
    var darkMode: Int
        get() = sp.getInt("dark_mode", 0)
        set(v) = sp.edit().putInt("dark_mode", v).apply()

    val userId: String
        get() = sp.getString("user_id", null) ?: run {
            val id = (20000000..29999999).random().toString()
            sp.edit().putString("user_id", id).apply()
            id
        }

    private fun dayKey(base: String) = "${base}_${todayKey()}"

    /** 清理 N 天前的每日打卡键，防止 SharedPreferences 无限累积 */
    fun pruneOldDayKeys(keepDays: Long = 7) {
        val cutoff = java.time.LocalDate.now().minusDays(keepDays)
            .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
        val stale = sp.all.keys.filter { k ->
            val day = k.substringAfterLast('_')
            (k.startsWith("water_") || k.startsWith("exercise_") || k.startsWith("fiber_")) &&
                day.length == 8 && day < cutoff
        }
        if (stale.isNotEmpty()) {
            sp.edit().apply { stale.forEach { remove(it) } }.apply()
        }
    }

    fun todayWater(): Int = sp.getInt(dayKey("water"), 0)
    fun setTodayWater(v: Int) = sp.edit().putInt(dayKey("water"), v).apply()

    fun todayExercise(): Int = sp.getInt(dayKey("exercise"), 0)
    fun setTodayExercise(v: Int) = sp.edit().putInt(dayKey("exercise"), v).apply()

    fun todayFiber(): Int = sp.getInt(dayKey("fiber"), 0)
    fun setTodayFiber(v: Int) = sp.edit().putInt(dayKey("fiber"), v).apply()
}
