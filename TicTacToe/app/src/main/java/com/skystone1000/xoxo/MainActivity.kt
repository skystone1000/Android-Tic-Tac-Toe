package com.skystone1000.xoxo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skystone1000.xoxo.data.settings.AppSettings
import com.skystone1000.xoxo.data.settings.ThemeMode
import com.skystone1000.xoxo.di.appContainer
import com.skystone1000.xoxo.ui.navigation.TicTacNavHost
import com.skystone1000.xoxo.ui.theme.TicTacTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val container = appContainer()
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            TicTacTheme(darkTheme = darkTheme) {
                TicTacNavHost()
            }
        }
    }
}
