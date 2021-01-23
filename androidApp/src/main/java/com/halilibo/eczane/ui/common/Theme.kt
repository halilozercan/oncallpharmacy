package com.halilibo.eczane.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.halilibo.shared.model.ThemePreference
import com.halilibo.shared.repository.SettingsRepository
import org.koin.core.context.KoinContextHandler

private val DarkColorPalette = darkColors(
    primary = Color(0xFFC62828),
    primaryVariant = Color(0xFFB71C1C),
    secondary = Color(0xFF3949AB),
    background = Color(0xFF242424)
)

private val LightColorPalette = lightColors(
    primary = Color(0xFFC62828),
    primaryVariant = Color(0xFFB71C1C),
    secondary = Color(0xFF3949AB),
    secondaryVariant = Color(0xFF283593),
    background = Color(0xFFF1F1F1),
    surface = Color.White
)

@Composable
fun OnCallPharmacyTheme(
    content: @Composable() () -> Unit
) {
    val settingsRepository: SettingsRepository = remember {
        KoinContextHandler.get().get()
    }

    val themePreference by settingsRepository
        .getThemePreference()
        .collectAsState(initial = ThemePreference.SYSTEM)

    val isDarkTheme = when(themePreference) {
        ThemePreference.SYSTEM -> { isSystemInDarkTheme() }
        ThemePreference.LIGHT -> { false }
        ThemePreference.DARK -> { true }
    }

    val colors = if (isDarkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    Providers(AmbientDarkTheme provides isDarkTheme) {
        MaterialTheme(
            colors = colors,
            content = content,
            typography = Typography(
                button = TextStyle(
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    letterSpacing = 1.25.sp
                )
            )
        )
    }
}

val AmbientDarkTheme = ambientOf<Boolean> { error("No providers") }