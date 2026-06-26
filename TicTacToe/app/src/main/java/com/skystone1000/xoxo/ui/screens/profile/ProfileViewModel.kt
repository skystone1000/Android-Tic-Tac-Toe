package com.skystone1000.xoxo.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skystone1000.xoxo.data.settings.SettingsRepository
import com.skystone1000.xoxo.data.stats.StatsRepository
import com.skystone1000.xoxo.data.stats.StatsSummary
import com.skystone1000.xoxo.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "Player",
    val summary: StatsSummary = StatsSummary(),
)

class ProfileViewModel(
    private val settingsRepository: SettingsRepository,
    statsRepository: StatsRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> =
        combine(settingsRepository.settings, statsRepository.summary) { settings, summary ->
            ProfileUiState(name = settings.playerName, summary = summary)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setName(name: String) = viewModelScope.launch {
        settingsRepository.setPlayerName(name.ifBlank { "Player" })
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { ProfileViewModel(container.settingsRepository, container.statsRepository) }
        }
    }
}
