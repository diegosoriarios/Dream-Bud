package com.diego.dreambud.data.repository

import com.diego.dreambud.data.local.DreamDao
import com.diego.dreambud.data.model.DreamEntry
import kotlinx.coroutines.flow.Flow

class DreamRepository(private val dreamDao: DreamDao) {
    fun getAllDreams(): Flow<List<DreamEntry>> = dreamDao.getAllDreams()

    fun getDreamById(id: Long): Flow<DreamEntry?> = dreamDao.getDreamById(id)

    fun searchDreams(query: String): Flow<List<DreamEntry>> = dreamDao.searchDreams(query)

    suspend fun insertDream(dream: DreamEntry): Long = dreamDao.insertDream(dream)

    suspend fun updateDream(dream: DreamEntry) = dreamDao.updateDream(dream)

    suspend fun deleteDream(dream: DreamEntry) = dreamDao.deleteDream(dream)

    suspend fun deleteDreamById(id: Long) = dreamDao.deleteDreamById(id)
}
