package com.example.tictactoe.data.stats

import com.example.tictactoe.domain.model.Difficulty
import com.example.tictactoe.domain.model.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Aggregated stats shown on the Stats screen. */
data class StatsSummary(
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val winRatePercent: Int = 0,
    val currentStreak: Int = 0,
    /** Match counts for the last 7 days, oldest first (size 7). */
    val last7Days: List<Int> = List(7) { 0 },
    val recent: List<MatchEntity> = emptyList(),
) {
    val totalGames: Int get() = wins + losses + draws
}

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

class StatsRepository(
    private val dao: MatchDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    val summary: Flow<StatsSummary> = dao.observeAll().map { toSummary(it) }

    suspend fun record(mode: GameMode, result: MatchResult, difficulty: Difficulty?) {
        dao.insert(
            MatchEntity(
                mode = mode.name,
                result = result.name,
                difficulty = difficulty?.name,
                playedAt = now(),
            ),
        )
    }

    suspend fun clear() = dao.clear()

    // matches arrive newest-first (DAO orders by playedAt DESC).
    private fun toSummary(matches: List<MatchEntity>): StatsSummary {
        val wins = matches.count { it.result == MatchResult.WIN.name }
        val losses = matches.count { it.result == MatchResult.LOSS.name }
        val draws = matches.count { it.result == MatchResult.DRAW.name }
        val total = matches.size
        val winRate = if (total > 0) (wins * 100) / total else 0

        var streak = 0
        for (m in matches) {
            if (m.result == MatchResult.WIN.name) streak++ else break
        }

        val todayBucket = now() / DAY_MILLIS
        val buckets = IntArray(7)
        for (m in matches) {
            val age = (todayBucket - m.playedAt / DAY_MILLIS).toInt()
            if (age in 0..6) buckets[6 - age]++ // index 6 = today, 0 = six days ago
        }

        return StatsSummary(
            wins = wins,
            losses = losses,
            draws = draws,
            winRatePercent = winRate,
            currentStreak = streak,
            last7Days = buckets.toList(),
            recent = matches.take(10),
        )
    }
}
