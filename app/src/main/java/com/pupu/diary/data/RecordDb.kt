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
                "duration INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL("CREATE INDEX idx_records_time ON records(time)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE records ADD COLUMN note TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE records ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun all(): List<PoopRecord> {
        val out = mutableListOf<PoopRecord>()
        readableDatabase.rawQuery(
            "SELECT _id, time, bristol, mood, smoothness, note, duration FROM records ORDER BY time DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                out.add(
                    PoopRecord(
                        id = c.getLong(0),
                        time = c.getLong(1),
                        bristol = c.getInt(2),
                        mood = c.getInt(3),
                        smoothness = c.getInt(4),
                        note = c.getString(5) ?: "",
                        durationMin = c.getInt(6)
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
    }

    companion object {
        private const val DB_VERSION = 3
    }
}
