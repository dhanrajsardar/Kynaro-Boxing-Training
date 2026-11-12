package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KynaroDarkColorScheme = darkColorScheme(
    primary = KynaroRed,
    onPrimary = KynaroBlack,
    primaryContainer = KynaroCardElevated,
    onPrimaryContainer = KynaroRed,
    secondary = KynaroTextWhite,
    onSecondary = KynaroBlack,
    secondaryContainer = KynaroCardElevated,
    onSecondaryContainer = KynaroTextWhite,
    tertiary = KynaroGreen,
    background = KynaroBlack,
    onBackground = KynaroTextWhite,
    surface = KynaroCardBg,
    onSurface = KynaroTextWhite,
    surfaceVariant = KynaroCardElevated,
    onSurfaceVariant = KynaroTextSecondary,
    outline = KynaroCardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KynaroDarkColorScheme,
        typography = Typography,
        content = content
    )
}
