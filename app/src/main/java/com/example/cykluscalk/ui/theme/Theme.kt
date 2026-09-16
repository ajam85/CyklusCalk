package com.example.cykluscalk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    secondary = Color(0xFFBBC7DB),
    tertiary = Color(0xFFD6BEE4)
)

private val BlueLightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary
)

private val PinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    secondary = Color(0xFFDDC2C9),
    tertiary = Color(0xFFEEBBD0)
)

private val PinkLightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = PinkTertiary
)

@Composable
fun CyklusCalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: String = "BLUE",
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        "PINK" -> if (darkTheme) PinkDarkColorScheme else PinkLightColorScheme
        else -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
