package com.skystone1000

import android.app.Application
import com.skystone1000.di.AppContainer

class TicTacApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
