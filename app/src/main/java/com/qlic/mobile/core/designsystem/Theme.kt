package com.qlic.mobile.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val QlicLightColorScheme = lightColorScheme(
    primary = QlicPrimary,
    onPrimary = QlicOnPrimary,
    background = QlicBackground,
    onBackground = QlicTextPrimary,
    surface = QlicBackground,
    onSurface = QlicTextPrimary,
    surfaceVariant = QlicSurfaceVariant,
    onSurfaceVariant = QlicTextSecondary,
    outline = QlicOutline,
    error = QlicError,
)

@Composable
fun QlicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QlicLightColorScheme,
        typography = QlicTypography,
        content = content,
    )
}
