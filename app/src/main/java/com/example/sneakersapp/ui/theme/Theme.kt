package com.example.sneakersapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrownPrimary,
    secondary = BrownSecondary,
    tertiary = BrownLight,
    background = WhiteBg,
    surface = WhiteBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BlackText,
    onSurface = BlackText
)

@Composable
fun SneakersAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}