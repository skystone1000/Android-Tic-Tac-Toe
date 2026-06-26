package com.example.tictactoe.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.tictactoe.data.settings.SettingsRepository
import com.example.tictactoe.data.stats.AppDatabase
import com.example.tictactoe.data.stats.StatsRepository
import com.example.tictactoe.domain.ai.AiOpponent
import com.example.tictactoe.domain.ai.MinimaxAi
import com.example.tictactoe.domain.engine.GameEngine
import com.example.tictactoe.domain.model.Difficulty

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Manual dependency container, held by [com.example.tictactoe.TicTacApp].
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
