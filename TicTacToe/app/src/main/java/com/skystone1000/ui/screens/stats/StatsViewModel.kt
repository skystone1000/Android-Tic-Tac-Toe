package com.skystone1000.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.skystone1000.data.stats.StatsRepository
import com.skystone1000.data.stats.StatsSummary
import com.skystone1000.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(statsRepository: StatsRepository) : ViewModel() {

    val summary: StateFlow<StatsSummary> =
        statsRepository.summary.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsSummary(),
        )

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { StatsViewModel(container.statsRepository) }
        }
    }
}
