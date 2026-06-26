package com.skystone1000.xoxo.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skystone1000.xoxo.data.settings.SettingsRepository
import com.skystone1000.xoxo.data.stats.MatchResult
import com.skystone1000.xoxo.data.stats.StatsRepository
import com.skystone1000.xoxo.di.AppContainer
import com.skystone1000.xoxo.domain.ai.AiOpponent
import com.skystone1000.xoxo.domain.engine.GameEngine
import com.skystone1000.xoxo.domain.model.Difficulty
import com.skystone1000.xoxo.domain.model.GameMode
import com.skystone1000.xoxo.domain.model.GameState
import com.skystone1000.xoxo.domain.model.GameStatus
import com.skystone1000.xoxo.domain.model.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class GameUiState(
    val gameState: GameState = GameState(),
    val scoreX: Int = 0,
    val scoreO: Int = 0,
    val playerXName: String = "X",
    val playerOName: String = "O",
    val isAiThinking: Boolean = false,
    val hapticsEnabled: Boolean = true,
)

class GameViewModel(
    private val engine: GameEngine,
    aiFactory: (Difficulty) -> AiOpponent,
    private val statsRepository: StatsRepository,
    settingsRepository: SettingsRepository,
    private val mode: GameMode,
    private val difficulty: Difficulty,
    private val humanSymbol: Player,
) : ViewModel() {

    private val ai: AiOpponent? = if (mode == GameMode.VS_AI) aiFactory(difficulty) else null
    private val aiSymbol: Player? = if (mode == GameMode.VS_AI) humanSymbol.opponent() else null

    private val _ui = MutableStateFlow(initialState())
    val ui: StateFlow<GameUiState> = _ui.asStateFlow()

    private var recorded = false

    init {
        settingsRepository.settings
            .onEach { s -> _ui.value = _ui.value.copy(hapticsEnabled = s.hapticsEnabled) }
            .launchIn(viewModelScope)
        maybeAiOpening()
    }

    private fun initialState(): GameUiState {
        val (xName, oName) = when (mode) {
            GameMode.VS_AI -> if (humanSymbol == Player.X) "You" to "AI" else "AI" to "You"
            GameMode.PASS_AND_PLAY -> "Player X" to "Player O"
        }
        return GameUiState(playerXName = xName, playerOName = oName)
    }

    fun onTileClick(index: Int) {
        val current = _ui.value
        if (current.gameState.status != GameStatus.InProgress || current.isAiThinking) return
        if (mode == GameMode.VS_AI && current.gameState.currentPlayer != humanSymbol) return

        val next = engine.move(current.gameState, index)
        if (next == current.gameState) return // illegal move
        _ui.value = current.copy(gameState = next)
        afterMove()
    }

    private fun afterMove() {
        val state = _ui.value.gameState
        if (state.status != GameStatus.InProgress) {
            finishRound(state.status)
        } else if (aiSymbol != null && state.currentPlayer == aiSymbol) {
            playAiMove()
        }
    }

    private fun playAiMove() {
        val opponent = ai ?: return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isAiThinking = true)
            delay(450)
            val state = _ui.value.gameState
            if (state.status == GameStatus.InProgress && state.currentPlayer == aiSymbol) {
                val move = opponent.chooseMove(state)
                _ui.value = _ui.value.copy(gameState = engine.move(state, move), isAiThinking = false)
                afterMove()
            } else {
                _ui.value = _ui.value.copy(isAiThinking = false)
            }
        }
    }

    private fun maybeAiOpening() {
        if (aiSymbol != null && _ui.value.gameState.currentPlayer == aiSymbol) playAiMove()
    }

    private fun finishRound(status: GameStatus) {
        if (recorded) return
        recorded = true
        val winner = (status as? GameStatus.Won)?.player
        _ui.value = _ui.value.copy(
            scoreX = _ui.value.scoreX + if (winner == Player.X) 1 else 0,
            scoreO = _ui.value.scoreO + if (winner == Player.O) 1 else 0,
        )

        val result = when {
            winner == null -> MatchResult.DRAW
            mode == GameMode.VS_AI -> if (winner == humanSymbol) MatchResult.WIN else MatchResult.LOSS
            else -> if (winner == Player.X) MatchResult.WIN else MatchResult.LOSS // pass&play tracks the X side
        }
        viewModelScope.launch {
            statsRepository.record(mode, result, if (mode == GameMode.VS_AI) difficulty else null)
        }
    }

    fun restart() {
        recorded = false
        _ui.value = _ui.value.copy(gameState = engine.reset(), isAiThinking = false)
        maybeAiOpening()
    }

    companion object {
        fun factory(
            container: AppContainer,
            mode: GameMode,
            difficulty: Difficulty,
            humanSymbol: Player,
        ) = viewModelFactory {
            initializer {
                GameViewModel(
                    engine = container.gameEngine,
                    aiFactory = container.aiFactory,
                    statsRepository = container.statsRepository,
                    settingsRepository = container.settingsRepository,
                    mode = mode,
                    difficulty = difficulty,
                    humanSymbol = humanSymbol,
                )
            }
        }
    }
}
