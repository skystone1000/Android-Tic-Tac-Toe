package com.skystone1000.xoxo.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import com.skystone1000.xoxo.TicTacApp

/** Convenience accessor for the app's [AppContainer] from any composable. */
@Composable
@ReadOnlyComposable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as TicTacApp).container
