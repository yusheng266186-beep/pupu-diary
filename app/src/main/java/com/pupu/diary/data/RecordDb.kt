package com.pupu.diary.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RecordDb(context: Context) :
    SQLiteOpenHelper(context, "pupu.db", null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE records(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time INTEGER NOT NULL," +
                "bristol INTEGER NOT NULL," +
                "mood INTEGER NOT NULL," +
                "smoothness INTEGER NOT NULL," +
                "note TEXT NOT NULL DEFAULT ''," +
                "duration INTEGER NOT NULL DEFAULT 0," +
                "colorType INTEGER," +
                "abdominalPain INTEGER NOT NULL DEFAULT 0," +
                "straining INTEGER NOT NULL DEFAULT 0," +
                "incomplete INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL("CREATE INDEX idx_records_time ON records(time)")
        db.execSQL(
            "CREATE TABLE checkins(" +
                "day TEXT PRIMARY KEY," +
                "water INTEGER NOT NULL DEFAULT 0," +
                "exercise INTEGER NOT NULL DEFAULT 0," +
                "fiber INTEGER NOT NULL DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE records ADD COLUMN note TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE records ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 4) {
            // v2.2.0：颜色日记 + 三项临床状态（便便晴天同源字段），旧数据留空即可
            db.execSQL("ALTER TABLE records ADD COLUMN colorType INTEGER")
            db.execSQL("ALTER TABLE records ADD COLUMN abdominalPain INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE records ADD COLUMN straining INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE records ADD COLUMN incomplete INTEGER NOT NULL DEFAULT 0")
        }
        if (oldVersion < 5) {
            // v4.0.0：每日打卡入表（水/运动/纤维），让打卡数据可积累、可做关联洞察
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS checkins(" +
                    "day TEXT PRIMARY KEY," +
                    "water INTEGER NOT NULL DEFAULT 0," +
                    "exercise INTEGER NOT NULL DEFAULT 0," +
                    "fiber INTEGER NOT NULL DEFAULT 0)"
            )
        }
    }

    /** 打卡 upsert（INSERT OR REPLACE 兼容 minSdk 26，不用 ON CONFLICT） */
    fun upsertCheckin(day: String, water: Int, exercise: Int, fiber: Int) {
        writableDatabase.execSQL(
            "INSERT OR REPLACE INTO checkins(day, water, exercise, fiber) VALUES(?,?,?,?)",
            arrayOf(day, water, exercise, fiber)
        )
    }

    fun loadCheckins(): Map<String, IntArray> {
        val out = HashMap<String, IntArray>()
        readableDatabase.rawQuery("SELECT day, water, exercise, fiber FROM checkins", null).use { c ->
            while (c.moveToNext()) {
                out[c.getString(0)] = intArrayOf(c.getInt(1), c.getInt(2), c.getInt(3))
            }
        }
        return out
    }

    fun all(): List<PoopRecord> {
        val out = mutableListOf<PoopRecord>()
        readableDatabase.rawQuery(
            "SELECT * FROM records ORDER BY time DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val colorIdx = c.getColumnIndexOrThrow("colorType")
                out.add(
                    PoopRecord(
                        id = c.getLong(c.getColumnIndexOrThrow("_id")),
                        time = c.getLong(c.getColumnIndexOrThrow("time")),
                        bristol = c.getInt(c.getColumnIndexOrThrow("bristol")),
                        mood = c.getInt(c.getColumnIndexOrThrow("mood")),
                        smoothness = c.getInt(c.getColumnIndexOrThrow("smoothness")),
                        note = c.getString(c.getColumnIndexOrThrow("note")) ?: "",
                        durationMin = c.getInt(c.getColumnIndexOrThrow("duration")),
                        colorType = if (c.isNull(colorIdx)) null else c.getInt(colorIdx),
                        abdominalPain = c.getInt(c.getColumnIndexOrThrow("abdominalPain")) != 0,
                        straining = c.getInt(c.getColumnIndexOrThrow("straining")) != 0,
                        incomplete = c.getInt(c.getColumnIndexOrThrow("incomplete")) != 0
                    )
                )
            }
        }
        return out
    }

    fun allTimes(): List<Long> {
        val out = mutableListOf<Long>()
        readableDatabase.rawQuery("SELECT time FROM records", null).use { c ->
            while (c.moveToNext()) out.add(c.getLong(0))
        }
        return out
    }

    fun insert(r: PoopRecord): Long {
        return writableDatabase.insert("records", null, toValues(r))
    }

    fun update(r: PoopRecord) {
        writableDatabase.update("records", toValues(r), "_id = ?", arrayOf(r.id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("records", "_id = ?", arrayOf(id.toString()))
    }

    fun clearAll() {
        writableDatabase.delete("records", null, null)
    }

    private fun toValues(r: PoopRecord) = ContentValues().apply {
        put("time", r.time)
        put("bristol", r.bristol)
        put("mood", r.mood)
        put("smoothness", r.smoothness)
        put("note", r.note)
        put("duration", r.durationMin)
        put("colorType", r.colorType)
        put("abdominalPain", if (r.abdominalPain) 1 else 0)
        put("straining", if (r.straining) 1 else 0)
        put("incomplete", if (r.incomplete) 1 else 0)
    }

    companion object {
        private const val DB_VERSION = 5
    }
}
