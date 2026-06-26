package com.skystone1000.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skystone1000.di.AppContainer
import com.skystone1000.di.appContainer
import com.skystone1000.domain.model.Difficulty
import com.skystone1000.domain.model.GameMode
import com.skystone1000.domain.model.Player
import com.skystone1000.ui.screens.game.GameScreen
import com.skystone1000.ui.screens.game.GameViewModel
import com.skystone1000.ui.screens.home.HomeScreen
import com.skystone1000.ui.screens.home.HomeViewModel
import com.skystone1000.ui.screens.onboarding.OnboardingScreen
import com.skystone1000.ui.screens.profile.ProfileScreen
import com.skystone1000.ui.screens.profile.ProfileViewModel
import com.skystone1000.ui.screens.setup.MatchSetupScreen
import com.skystone1000.ui.screens.settings.SettingsScreen
import com.skystone1000.ui.screens.settings.SettingsViewModel
import com.skystone1000.ui.screens.splash.SplashScreen
import com.skystone1000.ui.screens.stats.StatsScreen
import com.skystone1000.ui.screens.stats.StatsViewModel
import kotlinx.coroutines.launch

@Composable
fun TicTacNavHost() {
    val container = appContainer()
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(navController = nav, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            SplashScreen(
                ready = settings != null,
                onContinue = {
                    val dest = if (settings?.hasSeenOnboarding == true) Routes.HOME else Routes.ONBOARDING
                    nav.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
                },
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    scope.launch { container.settingsRepository.setOnboardingSeen(true) }
                    nav.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
                },
            )
        }

        // ---- Bottom-nav tabs ----
        tab(Routes.HOME, HomeTab.HOME, nav) { padding ->
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
            val state by vm.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                state = state,
                contentPadding = padding,
                onPassAndPlay = { nav.navigate(Routes.setup(GameMode.PASS_AND_PLAY, Difficulty.MEDIUM)) },
                onVsAi = { difficulty -> nav.navigate(Routes.setup(GameMode.VS_AI, difficulty)) },
                onOpenSettings = { navigateTab(nav, Routes.SETTINGS) },
                onOpenProfile = { navigateTab(nav, Routes.PROFILE) },
            )
        }

        tab(Routes.STATS, HomeTab.STATS, nav) { padding ->
            val vm: StatsViewModel = viewModel(factory = StatsViewModel.factory(container))
            val summary by vm.summary.collectAsStateWithLifecycle()
            StatsScreen(summary = summary, contentPadding = padding)
        }

        tab(Routes.PROFILE, HomeTab.PROFILE, nav) { padding ->
            val vm: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(container))
            val state by vm.uiState.collectAsStateWithLifecycle()
            ProfileScreen(state = state, contentPadding = padding, onSaveName = vm::setName)
        }

        tab(Routes.SETTINGS, HomeTab.SETTINGS, nav) { padding ->
            val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
            val settings by vm.settings.collectAsStateWithLifecycle()
            SettingsScreen(
                settings = settings,
                contentPadding = padding,
                onSound = vm::setSound,
                onHaptics = vm::setHaptics,
                onDarkMode = vm::setDarkMode,
                onDifficulty = vm::setDifficulty,
                onBoardTheme = vm::setBoardTheme,
            )
        }

        // ---- Match setup ----
        composable(
            Routes.SETUP,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
            ),
        ) { entry ->
            val mode = GameMode.valueOf(entry.arguments!!.getString("mode")!!)
            val difficulty = Difficulty.valueOf(entry.arguments!!.getString("difficulty")!!)
            MatchSetupScreen(
                mode = mode,
                initialDifficulty = difficulty,
                onStart = { symbol, diff ->
                    nav.navigate(Routes.game(mode, diff, symbol)) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() },
            )
        }

        // ---- Game ----
        composable(
            Routes.GAME,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("symbol") { type = NavType.StringType },
            ),
        ) { entry ->
            val mode = GameMode.valueOf(entry.arguments!!.getString("mode")!!)
            val difficulty = Difficulty.valueOf(entry.arguments!!.getString("difficulty")!!)
            val symbol = Player.valueOf(entry.arguments!!.getString("symbol")!!)
            val vm: GameViewModel = viewModel(
                factory = GameViewModel.factory(container, mode, difficulty, symbol),
            )
            val state by vm.ui.collectAsStateWithLifecycle()
            val modeLabel = if (mode == GameMode.VS_AI) {
                "Vs AI · ${difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}"
            } else {
                "Pass & Play"
            }
            GameScreen(
                modeLabel = modeLabel,
                isVsAi = mode == GameMode.VS_AI,
                humanSymbol = symbol,
                state = state,
                onTileClick = vm::onTileClick,
                onRestart = vm::restart,
                onQuit = { nav.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
    }
}

/** Registers a bottom-nav tab route, wrapping its content in [MainScaffold]. */
private fun NavGraphBuilder.tab(
    route: String,
    tab: HomeTab,
    nav: NavHostController,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    composable(route) {
        MainScaffold(
            current = tab,
            onSelect = { selected -> navigateTab(nav, selected.route) },
            content = content,
        )
    }
}

private fun navigateTab(nav: NavHostController, route: String) {
    nav.navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
