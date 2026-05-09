package com.queryb.medlog.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary          = MedBlue,
    onPrimary        = Color.White,
    primaryContainer = MedBlueLight,
    secondary        = MedGreen,
    onSecondary      = Color.White,
    background       = SurfaceWhite,
    surface          = CardWhite,
    onBackground     = TextDark,
    onSurface        = TextDark,
    error            = ErrorRed
)

@Composable
fun MedLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
