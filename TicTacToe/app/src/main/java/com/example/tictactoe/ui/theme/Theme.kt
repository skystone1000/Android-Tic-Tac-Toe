package com.example.tictactoe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Minimal wrapper for Phase 0. Phase 1 replaces this with full design-system tokens.
@Composable
fun TicTacTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
