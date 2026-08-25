package com.shunshun.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poop_records")
data class PoopRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val stoolType: Int,
    val mood: Int,
    val smoothness: Int,
    val durationMinutes: Int? = null,
    val abdominalPain: Boolean = false,
    val straining: Boolean = false,
    val incompleteFeeling: Boolean = false,
    val note: String = ""
)
