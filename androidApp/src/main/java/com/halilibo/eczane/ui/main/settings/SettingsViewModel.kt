package com.halilibo.eczane.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halilibo.eczane.ui.common.StatefulViewModel
import com.halilibo.shared.model.ThemePreference
import com.halilibo.shared.repository.SettingsRepository
import kotlinx.coroutines.flow.*

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : StatefulViewModel<SettingsState>(SettingsState()) {

    init {
        settingsRepository.getThemePreference()
            .onEach { themePreference ->
                setState {
                    it.copy(themePreference = themePreference)
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateThemePreference(themePreference: ThemePreference) {
        settingsRepository.saveThemePreference(themePreference)
    }
}

data class SettingsState(
    val themePreference: ThemePreference? = null
)
