package com.example.tictactoe.di

import android.content.Context

/**
 * Manual dependency container. Grows as later phases add the game engine,
 * AI factory, and repositories. Held by [com.example.tictactoe.TicTacApp].
 */
class AppContainer(private val appContext: Context)
