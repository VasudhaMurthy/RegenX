package com.example.regenx.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
// 🌟 IMPORTS: We import the colors defined in Color.kt instead of redefining them 🌟
// Ensure Purple80, Purple40, DarkBackground, DarkSurface, White, and Black are defined in Color.kt

// --- 🎨 DARK COLOR SCHEME ---
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground, // Use imported variable
    surface = DarkSurface,      // Use imported variable
    onSurface = White,          // Use imported variable (Light text)
    onBackground = White        // Light text
)

// --- 🎨 LIGHT COLOR SCHEME ---
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = White,         // Use imported variable
    surface = White,            // Use imported variable
    onSurface = Black,          // Use imported variable (Dark text)
    onBackground = Black        // Dark text
)

@Composable
fun RegenXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 🌟 FIX: Use SideEffect to update the system UI colors 🌟
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        SideEffect {
            // 1. Set Status Bar color to match the surface
            window.statusBarColor = colorScheme.surface.toArgb()

            // 2. Control the icons on the status bar (for Light/Dark mode appearance)
            // This is what makes the icons (clock, battery) dark on a light background and vice versa.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assumes Typography is available/imported
        content = content
    )
}