package com.example.tictactoe

import android.app.Application
import com.example.tictactoe.di.AppContainer

class TicTacApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
