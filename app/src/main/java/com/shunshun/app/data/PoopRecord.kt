package com.shunshun.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poop_records")
data class PoopRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val stoolType: Int,
    /** Optional diary-only color note. It is intentionally separate from stool shape. */
    val colorType: Int? = null,
    val mood: Int,
    val smoothness: Int,
    val durationMinutes: Int? = null,
    val abdominalPain: Boolean = false,
    val straining: Boolean = false,
    val incompleteFeeling: Boolean = false,
    val note: String = ""
)
