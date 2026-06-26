package com.skystone1000.xoxo.data.stats

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Result of a finished round, from the tracked player's perspective. */
enum class MatchResult { WIN, LOSS, DRAW }

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,          // GameMode name
    val result: String,        // MatchResult name
    val difficulty: String?,   // Difficulty name, null for Pass & Play
    val playedAt: Long,        // epoch millis
)
