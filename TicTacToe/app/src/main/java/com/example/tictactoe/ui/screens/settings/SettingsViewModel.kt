package com.example.tictactoe.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tictactoe.data.settings.AppSettings
import com.example.tictactoe.data.settings.BoardTheme
import com.example.tictactoe.data.settings.SettingsRepository
import com.example.tictactoe.data.settings.ThemeMode
import com.example.tictactoe.di.AppContainer
import com.example.tictactoe.domain.model.Difficulty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setSound(enabled: Boolean) = viewModelScope.launch { repository.setSound(enabled) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { repository.setHaptics(enabled) }
    fun setDarkMode(dark: Boolean) = viewModelScope.launch {
        repository.setThemeMode(if (dark) ThemeMode.DARK else ThemeMode.LIGHT)
    }
    fun setDifficulty(d: Difficulty) = viewModelScope.launch { repository.setDefaultDifficulty(d) }
    fun setBoardTheme(t: BoardTheme) = viewModelScope.launch { repository.setBoardTheme(t) }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container.settingsRepository) }
        }
    }
}
