package com.skystone1000.xoxo.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skystone1000.xoxo.di.AppContainer
import com.skystone1000.xoxo.data.settings.SettingsRepository
import com.skystone1000.xoxo.data.stats.StatsRepository
import com.skystone1000.xoxo.domain.model.Difficulty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val playerName: String = "Player",
    val streak: Int = 0,
    val defaultDifficulty: Difficulty = Difficulty.MEDIUM,
)

class HomeViewModel(
    settingsRepository: SettingsRepository,
    statsRepository: StatsRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(settingsRepository.settings, statsRepository.summary) { settings, stats ->
            HomeUiState(
                playerName = settings.playerName,
                streak = stats.currentStreak,
                defaultDifficulty = settings.defaultDifficulty,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { HomeViewModel(container.settingsRepository, container.statsRepository) }
        }
    }
}
