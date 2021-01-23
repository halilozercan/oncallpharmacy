package com.halilibo.shared.repository

import co.touchlab.kermit.Kermit
import com.halilibo.eczane.db.OnCallPharmacyDatabase
import com.halilibo.shared.model.ThemePreference
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.KoinComponent
import org.koin.core.inject


class SettingsRepository() : KoinComponent {
    private val logger: Kermit by inject()
    private val onCallPharmacyDatabase: OnCallPharmacyDatabase by inject()

    // TODO(halilozercan): Planned use
    // private val settings: Settings = Settings()

    fun getThemePreference(): Flow<ThemePreference> {
        return onCallPharmacyDatabase.onCallPharmacyQueries.getThemePreference()
            .asFlow()
            .mapToOne()
            .map { ordinal ->
                val values = ThemePreference.values()
                if (ordinal < values.size) {
                    values[ordinal.toInt()]
                } else {
                    logger.e { "Unexpected ordinal '${ordinal}' found in settings for Theme Preference" }
                    values[0]
                }
            }
    }

    fun saveThemePreference(themePreference: ThemePreference) {
        onCallPharmacyDatabase.onCallPharmacyQueries.updateThemePreference(
            ordinal = themePreference.ordinal.toLong()
        )
    }

    companion object {
        // private const val THEME_PREFERENCE_KEY = "THEME_PREFERENCE_KEY"
    }
}

