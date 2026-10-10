package com.diego.dreambud.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.diego.dreambud.data.model.DreamEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {
    @Query("SELECT * FROM dreams ORDER BY timestamp DESC")
    fun getAllDreams(): Flow<List<DreamEntry>>

    @Query("SELECT * FROM dreams WHERE id = :id")
    fun getDreamById(id: Long): Flow<DreamEntry?>

    @Query("SELECT * FROM dreams WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchDreams(query: String): Flow<List<DreamEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDream(dream: DreamEntry): Long

    @Update
    suspend fun updateDream(dream: DreamEntry)

    @Delete
    suspend fun deleteDream(dream: DreamEntry)

    @Query("DELETE FROM dreams WHERE id = :id")
    suspend fun deleteDreamById(id: Long)
}
