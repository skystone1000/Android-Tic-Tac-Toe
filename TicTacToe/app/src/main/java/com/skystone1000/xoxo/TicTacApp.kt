package com.skystone1000.xoxo

import android.app.Application
import com.skystone1000.xoxo.di.AppContainer

class TicTacApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
