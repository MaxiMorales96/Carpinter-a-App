package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WoodPrimaryDark,
    secondary = WoodSecondaryDark,
    tertiary = WoodTertiaryDark,
    background = WoodBackgroundDark,
    surface = WoodSurfaceDark,
    onPrimary = Color(0xFF2E2724),
    onSecondary = Color(0xFF2E2724),
    onTertiary = Color.White,
    onBackground = WoodTextDark,
    onSurface = WoodTextDark
)

private val LightColorScheme = lightColorScheme(
    primary = WoodPrimary,
    secondary = WoodSecondary,
    tertiary = WoodTertiary,
    background = WoodBackground,
    surface = WoodSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF2E2724),
    onBackground = WoodTextLight,
    onSurface = WoodTextLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color to maintain our custom wood aesthetic!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
