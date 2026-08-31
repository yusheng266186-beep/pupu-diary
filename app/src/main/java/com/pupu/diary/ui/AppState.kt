package com.pupu.diary.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** 应用级状态：记录、偏好、每日打卡（进程级实例，见 PupuApp） */
class AppState(context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appContext = context.applicationContext
    private val db = RecordDb(appContext)
    val prefs = Prefs(appContext)

    val records = mutableStateListOf<PoopRecord>()
    /** 打卡历史 day(yyyyMMdd) -> [water, exercise, fiber]，v4.0.0 起沉淀进 DB v5 */
    val checkins = mutableStateMapOf<String, IntArray>()
    var loaded by mutableStateOf(false)
        private set
    var themeIndex by mutableIntStateOf(prefs.themeIndex)
        private set
    var privacyMode by mutableStateOf(prefs.privacyMode)
        private set
    var autoBackupOn by mutableStateOf(prefs.autoBackupOn)
        private set

    /** 昵称（首页问候与我的页展示），空 = 未设置 */
    var userName: String by mutableStateOf(prefs.userName)
        private set

    /** 夜间模式：0 跟随系统 / 1 常开 / 2 常关 */
    var darkMode by mutableIntStateOf(prefs.darkMode)
        private set

    /** 最近一条待撤销删除的记录（撤销条 5 秒窗口） */
    var pendingUndo by mutableStateOf<PoopRecord?>(null)
        private set
    private var undoJob: kotlinx.coroutines.Job? = null

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
            val rows = db.all()
            val ck = db.loadCheckins()
            val w = prefs.todayWater()
            val ex = prefs.todayExercise()
            val fb = prefs.todayFiber()
            withContext(Dispatchers.Main) {
                records.addAll(rows)
                checkins.putAll(ck)
                water = w
                exercise = ex
                fiber = fb
                loaded = true
            }
            maybeAutoBackup()
        }
    }

    /** 内存列表只在主线程变更，DB 写在 IO 线程 */
    private fun sortedInsert(r: PoopRecord) {
        records.add(0, r)
        records.sortByDescending { it.time }
    }

    fun addRecord(
        bristol: Int,
        mood: Int,
        smoothness: Int,
        note: String = "",
        time: Long = System.currentTimeMillis(),
        durationMin: Int = 0,
        colorType: Int? = null,
        abdominalPain: Boolean = false,
        straining: Boolean = false,
        incomplete: Boolean = false
    ) {
        scope.launch {
            val r = PoopRecord(
                time = time, bristol = bristol, mood = mood,
                smoothness = smoothness, note = note, durationMin = durationMin,
                colorType = colorType, abdominalPain = abdominalPain,
                straining = straining, incomplete = incomplete
            )
            val id = db.insert(r)
            withContext(Dispatchers.Main) { sortedInsert(r.copy(id = id)) }
        }
    }

    fun updateRecord(r: PoopRecord) {
        scope.launch {
            db.update(r)
            withContext(Dispatchers.Main) {
                val idx = records.indexOfFirst { it.id == r.id }
                if (idx >= 0) records[idx] = r
                records.sortByDescending { it.time }
            }
        }
    }

    fun deleteRecord(r: PoopRecord) {
        scope.launch {
            db.delete(r.id)
            withContext(Dispatchers.Main) { records.removeAll { it.id == r.id } }
        }
    }

    /** 删除并给出 5 秒撤销窗口（撤销条），误删不打断 */
    fun removeWithUndo(r: PoopRecord) {
        deleteRecord(r)
        pendingUndo = r
        undoJob?.cancel()
        undoJob = scope.launch {
            delay(5000)
            withContext(Dispatchers.Main) {
                if (pendingUndo?.id == r.id) pendingUndo = null
            }
        }
    }

    /** 撤销最近一次删除：整条按原字段回插 */
    fun undoLastDelete() {
        val r = pendingUndo ?: return
        pendingUndo = null
        undoJob?.cancel()
        scope.launch {
            val id = db.insert(r)
            withContext(Dispatchers.Main) { sortedInsert(r.copy(id = id)) }
        }
    }

    /** 清空全部记录（设置里的危险操作，调用前需二次确认） */
    fun clearAllRecords() {
        scope.launch {
            db.clearAll()
            withContext(Dispatchers.Main) { records.clear() }
        }
    }

    /** 从备份 JSON 合并记录（按时间+形状去重），回调返回新增条数 */
    fun importFromJson(json: String, onDone: (Int) -> Unit) {
        scope.launch {
            val parsed = Backup.parse(json)
            val existing = db.all().map { "${it.time}|${it.bristol}" }.toHashSet()
            var added = 0
            parsed.forEach { r ->
                val key = "${r.time}|${r.bristol}"
                if (existing.add(key)) {
                    db.insert(r)
                    added++
                }
            }
            if (added > 0) {
                val rows = db.all()
                withContext(Dispatchers.Main) {
                    records.clear()
                    records.addAll(rows)
                }
            }
            withContext(Dispatchers.Main) { onDone(added) }
        }
    }

    fun setTheme(index: Int) {
        themeIndex = index
        prefs.themeIndex = index
    }

    fun updateUserName(name: String) {
        userName = name.trim().take(12)
        prefs.userName = userName
    }

    fun updateDarkMode(mode: Int) {
        darkMode = mode.coerceIn(0, 2)
        prefs.darkMode = darkMode
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

    /** 打卡：prefs 保持"今天"的即时读源，同时镜像进 DB 供历史关联分析 */
    fun addWater(n: Int = 1) = addCheckin(n = n, day = "water")
    fun addExercise(n: Int) = addCheckin(n = n, day = "exercise")
    fun addFiber(n: Int) = addCheckin(n = n, day = "fiber")

    private fun addCheckin(n: Int, day: String) {
        when (day) {
            "water" -> water = (water + n).coerceAtLeast(0)
            "exercise" -> exercise = (exercise + n).coerceAtLeast(0)
            "fiber" -> fiber = (fiber + n).coerceAtLeast(0)
        }
        val (w, ex, fb) = Triple(water, exercise, fiber)
        when (day) {
            "water" -> prefs.setTodayWater(w)
            "exercise" -> prefs.setTodayExercise(ex)
            "fiber" -> prefs.setTodayFiber(fb)
        }
        val key = todayKey()
        checkins[key] = intArrayOf(w, ex, fb)
        scope.launch { db.upsertCheckin(key, w, ex, fb) }
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

    /** 距上次自动备份 ≥7 天时静默备份到应用目录（v4.0.0 修复日期格式解析，周期间隔真正生效） */
    private fun maybeAutoBackup() {
        if (!autoBackupOn) return
        if (!Backup.isBackupDue(prefs.lastAutoBackup, LocalDate.now())) return
        val snapshot = records.toList()
        if (snapshot.isEmpty()) return
        try {
            Backup.autoBackup(appContext, snapshot)
            prefs.lastAutoBackup = todayKey()
        } catch (_: Exception) {
        }
    }
}
