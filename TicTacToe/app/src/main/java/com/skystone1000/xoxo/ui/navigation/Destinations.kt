package com.skystone1000.xoxo.ui.navigation

import com.skystone1000.xoxo.domain.model.Difficulty
import com.skystone1000.xoxo.domain.model.GameMode
import com.skystone1000.xoxo.domain.model.Player

/** Central route table. Arg routes expose typed builders. */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"

    const val SETUP = "setup/{mode}/{difficulty}"
    fun setup(mode: GameMode, difficulty: Difficulty) = "setup/${mode.name}/${difficulty.name}"

    const val GAME = "game/{mode}/{difficulty}/{symbol}"
    fun game(mode: GameMode, difficulty: Difficulty, symbol: Player) =
        "game/${mode.name}/${difficulty.name}/${symbol.name}"
}

/** Tabs shown in the bottom navigation bar. */
enum class HomeTab(val route: String, val label: String) {
    HOME(Routes.HOME, "Home"),
    STATS(Routes.STATS, "Stats"),
    PROFILE(Routes.PROFILE, "Profile"),
    SETTINGS(Routes.SETTINGS, "Settings"),
}
