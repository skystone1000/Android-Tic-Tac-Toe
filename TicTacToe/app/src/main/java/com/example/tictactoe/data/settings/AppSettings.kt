package com.example.tictactoe.data.settings

import com.example.tictactoe.domain.model.Difficulty

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Selectable board palettes shown in setup/settings. */
enum class BoardTheme { CLASSIC, MIDNIGHT, AURORA }

data class AppSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultDifficulty: Difficulty = Difficulty.MEDIUM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val playerName: String = "Player",
    val hasSeenOnboarding: Boolean = false,
)
