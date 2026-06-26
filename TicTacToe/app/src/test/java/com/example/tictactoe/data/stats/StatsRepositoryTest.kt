package com.example.tictactoe.data.stats

import com.example.tictactoe.domain.model.Difficulty
import com.example.tictactoe.domain.model.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsRepositoryTest {

    /** In-memory MatchDao backed by a StateFlow, ordered newest-first like the real query. */
    private class FakeMatchDao : MatchDao {
        private val items = MutableStateFlow<List<MatchEntity>>(emptyList())
        override suspend fun insert(match: MatchEntity) {
            items.value = (items.value + match).sortedByDescending { it.playedAt }
        }
        override fun observeAll(): Flow<List<MatchEntity>> = items
        override suspend fun clear() { items.value = emptyList() }
    }

    private val day = 24L * 60 * 60 * 1000
    private var clock = 10_000L * day // arbitrary "today"

    private fun repo() = StatsRepository(FakeMatchDao(), now = { clock })

    @Test
    fun `counts wins losses draws and win rate`() = runTest {
        val repo = repo()
        repo.record(GameMode.VS_AI, MatchResult.WIN, Difficulty.HARD)
        repo.record(GameMode.VS_AI, MatchResult.WIN, Difficulty.EASY)
        repo.record(GameMode.VS_AI, MatchResult.LOSS, Difficulty.HARD)
        repo.record(GameMode.PASS_AND_PLAY, MatchResult.DRAW, null)

        val s = repo.summary.first()
        assertEquals(2, s.wins)
        assertEquals(1, s.losses)
        assertEquals(1, s.draws)
        assertEquals(4, s.totalGames)
        assertEquals(50, s.winRatePercent) // 2 / 4
    }

    @Test
    fun `current streak counts consecutive recent wins only`() = runTest {
        val repo = repo()
        // Oldest -> newest by advancing the clock.
        repo.record(GameMode.VS_AI, MatchResult.LOSS, Difficulty.HARD); clock += 1
        repo.record(GameMode.VS_AI, MatchResult.WIN, Difficulty.HARD); clock += 1
        repo.record(GameMode.VS_AI, MatchResult.WIN, Difficulty.HARD); clock += 1
        repo.record(GameMode.VS_AI, MatchResult.WIN, Difficulty.HARD)

        assertEquals(3, repo.summary.first().currentStreak)
    }

    @Test
    fun `streak breaks on a non-win`() = runTest {
        val repo = repo()
        repo.record(GameMode.VS_AI, MatchResult.WIN, Difficulty.HARD); clock += 1
        repo.record(GameMode.VS_AI, MatchResult.LOSS, Difficulty.HARD)
        assertEquals(0, repo.summary.first().currentStreak)
    }

    @Test
    fun `last7Days buckets today into the final slot`() = runTest {
        val repo = repo()
        repo.record(GameMode.VS_AI, MatchResult.WIN, Difficulty.HARD)
        repo.record(GameMode.VS_AI, MatchResult.LOSS, Difficulty.HARD)
        val last7 = repo.summary.first().last7Days
        assertEquals(7, last7.size)
        assertEquals(2, last7[6]) // today
        assertEquals(0, last7[0])
    }
}
