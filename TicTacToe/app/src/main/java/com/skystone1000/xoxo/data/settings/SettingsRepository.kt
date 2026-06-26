package com.skystone1000.xoxo.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skystone1000.xoxo.domain.model.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val THEME = stringPreferencesKey("theme_mode")
        val DIFFICULTY = stringPreferencesKey("default_difficulty")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val ONBOARDED = booleanPreferencesKey("has_seen_onboarding")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { p ->
        AppSettings(
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            defaultDifficulty = p[Keys.DIFFICULTY]?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.MEDIUM,
            boardTheme = p[Keys.BOARD_THEME]?.let { runCatching { BoardTheme.valueOf(it) }.getOrNull() } ?: BoardTheme.CLASSIC,
            playerName = p[Keys.PLAYER_NAME] ?: "Player",
            hasSeenOnboarding = p[Keys.ONBOARDED] ?: false,
        )
    }

    suspend fun setSound(enabled: Boolean) = edit { it[Keys.SOUND] = enabled }
    suspend fun setHaptics(enabled: Boolean) = edit { it[Keys.HAPTICS] = enabled }
    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    suspend fun setDefaultDifficulty(d: Difficulty) = edit { it[Keys.DIFFICULTY] = d.name }
    suspend fun setBoardTheme(t: BoardTheme) = edit { it[Keys.BOARD_THEME] = t.name }
    suspend fun setPlayerName(name: String) = edit { it[Keys.PLAYER_NAME] = name }
    suspend fun setOnboardingSeen(seen: Boolean) = edit { it[Keys.ONBOARDED] = seen }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }
}
