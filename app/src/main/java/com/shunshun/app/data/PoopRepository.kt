package com.shunshun.app.data

import kotlinx.coroutines.flow.Flow

class PoopRepository(private val dao: PoopDao) {
    val records: Flow<List<PoopRecord>> = dao.observeAll()

    suspend fun add(record: PoopRecord) = dao.insert(record)
    suspend fun addAll(records: List<PoopRecord>) = dao.insertAll(records)
    suspend fun update(record: PoopRecord) = dao.update(record)
    suspend fun delete(record: PoopRecord) = dao.delete(record)
    suspend fun clear() = dao.clearAll()
}
