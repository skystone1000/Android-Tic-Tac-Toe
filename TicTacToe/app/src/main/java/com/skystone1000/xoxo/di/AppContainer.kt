package com.skystone1000.xoxo.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.skystone1000.xoxo.data.settings.SettingsRepository
import com.skystone1000.xoxo.data.stats.AppDatabase
import com.skystone1000.xoxo.data.stats.StatsRepository
import com.skystone1000.xoxo.domain.ai.AiOpponent
import com.skystone1000.xoxo.domain.ai.MinimaxAi
import com.skystone1000.xoxo.domain.engine.GameEngine
import com.skystone1000.xoxo.domain.model.Difficulty

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Manual dependency container, held by [com.skystone1000.xoxo.TicTacApp].
 * Keeps the graph trivial — no DI framework needed for this app.
 */
class AppContainer(appContext: Context) {

    val gameEngine: GameEngine = GameEngine()

    val aiFactory: (Difficulty) -> AiOpponent = { difficulty ->
        MinimaxAi(gameEngine, difficulty)
    }

    private val database: AppDatabase = AppDatabase.build(appContext)

    val statsRepository: StatsRepository = StatsRepository(database.matchDao())

    val settingsRepository: SettingsRepository = SettingsRepository(appContext.dataStore)
}
