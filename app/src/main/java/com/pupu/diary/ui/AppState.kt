package com.pupu.diary.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pupu.diary.data.PoopRecord
import com.pupu.diary.data.Prefs
import com.pupu.diary.data.RecordDb
import com.pupu.diary.util.Backup
import com.pupu.diary.util.Reminders
import com.pupu.diary.util.todayKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** 应用级状态：记录、偏好、每日打卡 */
class AppState(context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appContext = context.applicationContext
    private val db = RecordDb(appContext)
    val prefs = Prefs(appContext)

    val records = mutableStateListOf<PoopRecord>()
    var loaded by mutableStateOf(false)
        private set
    var themeIndex by mutableIntStateOf(prefs.themeIndex)
        private set
    var privacyMode by mutableStateOf(prefs.privacyMode)
        private set
    var autoBackupOn by mutableStateOf(prefs.autoBackupOn)
        private set

    var water by mutableIntStateOf(0)
        private set
    var exercise by mutableIntStateOf(0)
        private set
    var fiber by mutableIntStateOf(0)
        private set

    var recordRemindOn by mutableStateOf(prefs.recordRemindOn)
        private set
    var recordRemindMin by mutableIntStateOf(prefs.recordRemindMin)
        private set
    var waterRemindOn by mutableStateOf(prefs.waterRemindOn)
        private set
    var waterRemindMin by mutableIntStateOf(prefs.waterRemindMin)
        private set

    init {
        scope.launch {
            prefs.pruneOldDayKeys()
            records.addAll(db.all())
            water = prefs.todayWater()
            exercise = prefs.todayExercise()
            fiber = prefs.todayFiber()
            loaded = true
            maybeAutoBackup()
        }
    }

    fun addRecord(
        bristol: Int,
        mood: Int,
        smoothness: Int,
        note: String = "",
        time: Long = System.currentTimeMillis(),
        durationMin: Int = 0
    ) {
        scope.launch {
            val r = PoopRecord(
                time = time, bristol = bristol, mood = mood,
                smoothness = smoothness, note = note, durationMin = durationMin
            )
            val id = db.insert(r)
            records.add(0, r.copy(id = id))
            records.sortByDescending { it.time }
        }
    }

    fun updateRecord(r: PoopRecord) {
        scope.launch {
            db.update(r)
            val idx = records.indexOfFirst { it.id == r.id }
            if (idx >= 0) records[idx] = r
            records.sortByDescending { it.time }
        }
    }

    fun deleteRecord(r: PoopRecord) {
        scope.launch {
            db.delete(r.id)
            records.removeAll { it.id == r.id }
        }
    }

    /** 清空全部记录（设置里的危险操作，调用前需二次确认） */
    fun clearAllRecords() {
        scope.launch {
            db.clearAll()
            records.clear()
        }
    }

    /** 从备份 JSON 合并记录（按时间去重），回调返回新增条数 */
    fun importFromJson(json: String, onDone: (Int) -> Unit) {
        scope.launch {
            val parsed = Backup.parse(json)
            val existing = db.allTimes().toHashSet()
            var added = 0
            parsed.forEach { r ->
                if (r.time !in existing) {
                    db.insert(r)
                    existing.add(r.time)
                    added++
                }
            }
            if (added > 0) {
                records.clear()
                records.addAll(db.all())
            }
            withContext(Dispatchers.Main) { onDone(added) }
        }
    }

    fun setTheme(index: Int) {
        themeIndex = index
        prefs.themeIndex = index
    }

    fun setPrivacy(on: Boolean) {
        privacyMode = on
        prefs.privacyMode = on
    }

    fun setAutoBackup(on: Boolean) {
        autoBackupOn = on
        prefs.autoBackupOn = on
        if (on) maybeAutoBackup()
    }

    fun addWater(n: Int = 1) {
        water = (water + n).coerceAtLeast(0)
        prefs.setTodayWater(water)
    }

    fun addExercise(n: Int) {
        exercise = (exercise + n).coerceAtLeast(0)
        prefs.setTodayExercise(exercise)
    }

    fun addFiber(n: Int) {
        fiber = (fiber + n).coerceAtLeast(0)
        prefs.setTodayFiber(fiber)
    }

    fun setRecordReminder(context: Context, on: Boolean, min: Int) {
        recordRemindOn = on
        recordRemindMin = min
        prefs.recordRemindOn = on
        prefs.recordRemindMin = min
        if (on) {
            Reminders.schedule(
                context, Reminders.CODE_RECORD, min,
                "该噗噗啦～", "深呼吸，找个舒服的姿势，去迎接今天的轻松时刻吧"
            )
        } else {
            Reminders.cancel(context, Reminders.CODE_RECORD)
        }
    }

    fun setWaterReminder(context: Context, on: Boolean, min: Int) {
        waterRemindOn = on
        waterRemindMin = min
        prefs.waterRemindOn = on
        prefs.waterRemindMin = min
        if (on) {
            Reminders.schedule(
                context, Reminders.CODE_WATER, min,
                "喝口水吧 💧", "小口慢饮，给身体补补水"
            )
        } else {
            Reminders.cancel(context, Reminders.CODE_WATER)
        }
    }

    /** App 启动时重挂提醒（配合 BootReceiver 双保险） */
    fun reArmReminders(context: Context) {
        if (recordRemindOn) {
            Reminders.schedule(
                context, Reminders.CODE_RECORD, recordRemindMin,
                "该噗噗啦～", "深呼吸，找个舒服的姿势，去迎接今天的轻松时刻吧"
            )
        }
        if (waterRemindOn) {
            Reminders.schedule(
                context, Reminders.CODE_WATER, waterRemindMin,
                "喝口水吧 💧", "小口慢饮，给身体补补水"
            )
        }
    }

    /** 距上次自动备份 ≥7 天时静默备份到应用目录 */
    private fun maybeAutoBackup() {
        if (!autoBackupOn) return
        val due = try {
            val last = prefs.lastAutoBackup
            last.isEmpty() || LocalDate.parse(last).plusDays(7) <= LocalDate.now()
        } catch (_: Exception) {
            true
        }
        if (due && records.isNotEmpty()) {
            try {
                Backup.autoBackup(appContext, records.toList())
                prefs.lastAutoBackup = todayKey()
            } catch (_: Exception) {
            }
        }
    }
}
