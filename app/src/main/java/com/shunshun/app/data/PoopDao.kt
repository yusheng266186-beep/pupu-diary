package com.shunshun.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PoopDao {
    @Insert suspend fun insert(record: PoopRecord)
    @Insert suspend fun insertAll(records: List<PoopRecord>)
    @Delete suspend fun delete(record: PoopRecord)
    @Query("SELECT * FROM poop_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<PoopRecord>>
    @Query("SELECT * FROM poop_records WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<PoopRecord>>
    @Query("DELETE FROM poop_records") suspend fun clearAll()
}
