package com.skystone1000.xoxo.data.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: MatchEntity)

    @Query("SELECT * FROM matches ORDER BY playedAt DESC")
    fun observeAll(): Flow<List<MatchEntity>>

    @Query("DELETE FROM matches")
    suspend fun clear()
}
